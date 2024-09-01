const { Xumm } = require("xumm");
const { XrplClient } = require("xrpl-client");
const { Client } = require("xrpl");

class XRPLService {
  constructor() {
    this.xumm = new Xumm(process.env.XUMM_API_KEY, process.env.XUMM_API_SECRET);
    this.client = new XrplClient(process.env.TEST_NET);
    this.xrpl = new Client(process.env.TEST_NET);
  }

  // 로그인 할 때 사용되는 SignIn 트랜잭션
  // QR코드가 화면으로 전송되고 대기를 하다가
  // 핸드폰 어플로 로그인을 하면 완료가 된다.
  // 트랜잭션이 완료가 되면 지정된 주소로 리다이렉션이 된다.
  async SignIn(res) {
    let result;
    await this.xumm.payload
      .createAndSubscribe(
        {
          txjson: {
            TransactionType: "SignIn",
          },
          options: {
            return_url: {
              web: "http://localhost:5137",
            },
          },
        },
        (eventMessage) => {
          if (Object.keys(eventMessage.data).indexOf("opened") > -1) {
            // Update the UI? The payload was opened.
          }
          if (Object.keys(eventMessage.data).indexOf("signed") > -1) {
            // The `signed` property is present, true (signed) / false (rejected)
            return eventMessage;
          }
        }
      )
      .then(({ created, resolved }) => {
        console.log("Payload URL:", created.next.always);
        console.log("Payload QR:", created.refs.qr_png);
        res.send(created.next.always);

        return resolved; // Return payload promise for the next `then`
      })
      .then((payload) => {
        console.log("Payload resolved", payload);
        result = payload;
      });
    return result.payload.response.account;
  }

  // Grant 변화에 대한 내용들을 AccountSet 트랜잭션에 담아 보낸다.
  async AccountSet(grant, res) {
    let result;
    try {
      await this.xumm.payload
        .createAndSubscribe(
          {
            txjson: {
              TransactionType: "AccountSet",
              Account: grant.address,
              Memos: [
                {
                  Memo: {
                    MemoType: Buffer.from(grant.title)
                      .toString("hex")
                      .toUpperCase(),
                    MemoData: Buffer.from(JSON.stringify(grant)).toString(
                      "hex"
                    ),
                  },
                },
              ],
            },
            options: {
              return_url: {
                web: "http://localhost:5137",
              },
            },
          },
          (eventMessage) => {
            if (Object.keys(eventMessage.data).indexOf("opened") > -1) {
              // Update the UI? The payload was opened.
            }
            if (Object.keys(eventMessage.data).indexOf("signed") > -1) {
              // The `signed` property is present, true (signed) / false (rejected)
              return eventMessage;
            }
          }
        )
        .then(({ created, resolved }) => {
          console.log("Payload URL:", created.next.always);
          console.log("Payload QR:", created.refs.qr_png);
          res.send(created.next.always);

          return resolved; // Return payload promise for the next `then`
        })
        .then((payload) => {
          console.log("Payload resolved", payload);
          result = payload;
        });
    } catch (error) {
      console.log(error);
    }
    // 정상적으로 트랜잭션이 성공하지 못하면 에러 발생
    if (result.payload.response.dispatched_result === "") {
      throw new Error("Transaction Failed");
    }
  }

  // 사용자가 Grant에 기부를 할 때 바로 기부금을 전송하지 못하고
  // Grant 기간이 끝나면 기부를 가능하게 하기 위해 Escrow 트랜잭션을 활용
  // EscrowCreate 트랜잭션에서 조건을 시간으로 설정한다.
  async EscrowCreate(donation, res) {
    let result;
    try {
      await this.xumm.payload
        .createAndSubscribe(
          {
            txjson: {
              TransactionType: "EscrowCreate",
              Amount: donation.Amount, // "1000000" = 1XRP 문자열
              Account: donation.Account,
              Destination: donation.Destination,
              FinishAfter: donation.deadLine, // 숫자
            },
            options: {
              return_url: {
                web: "http://localhost:5137",
              },
            },
          },
          (eventMessage) => {
            if (Object.keys(eventMessage.data).indexOf("opened") > -1) {
              // Update the UI? The payload was opened.
            }
            if (Object.keys(eventMessage.data).indexOf("signed") > -1) {
              // The `signed` property is present, true (signed) / false (rejected)
              return eventMessage;
            }
          }
        )
        .then(({ created, resolved }) => {
          console.log("Payload URL:", created.next.always);
          console.log("Payload QR:", created.refs.qr_png);
          res.send(created.next.always);

          return resolved; // Return payload promise for the next `then`
        })
        .then((payload) => {
          console.log("Payload resolved", payload);
          result = payload;
        });
    } catch (error) {
      console.log(error);
    }

    // Sequence를 얻기 위한 API 요청
    // xrpl-client 라이브러리를 사용한다.
    const tx = await this.client.send({
      id: 1,
      command: "tx",
      transaction: result.payload.response.txid,
    });

    return tx.Sequence;
  }
  // 관리자가 Grant기간이 끝난 후 총 매칭풀에 있는 자금을
  // 각 그랜트 제안자에게 분배하기 위한 함수
  // 즉시 자금을 전달하기 위해 Payment 트랜잭션을 이용.
  async Payment(grants, pay, res) {
    // 관리자가 총매칭풀에서 자금을 분배하였다는 것을
    // AccountSet 트랜잭션을 이용한다.
    try {
      await this.xumm.payload
        .createAndSubscribe(
          {
            txjson: {
              TransactionType: "AccountSet",
              Account: pay.address,
              Memos: [
                {
                  Memo: {
                    MemoType: Buffer.from("Distribution")
                      .toString("hex")
                      .toUpperCase(),
                    MemoData: Buffer.from("Admin distributes funding").toString(
                      "hex"
                    ),
                  },
                },
              ],
            },
            options: {
              return_url: {
                web: "http://localhost:5137",
              },
            },
          },
          (eventMessage) => {
            if (Object.keys(eventMessage.data).indexOf("opened") > -1) {
              // Update the UI? The payload was opened.
            }
            if (Object.keys(eventMessage.data).indexOf("signed") > -1) {
              // The `signed` property is present, true (signed) / false (rejected)
              return eventMessage;
            }
          }
        )
        .then(({ created, resolved }) => {
          console.log("Payload URL:", created.next.always);
          console.log("Payload QR:", created.refs.qr_png);
          res.send(created.next.always);
          return resolved; // Return payload promise for the next `then`
        })
        .then((payload) => {
          console.log("Payload resolved", payload);
        });
    } catch (error) {
      console.log(error);
    }

    // 수 많은 Grant마다 관리자가 일일히 서명을 하기에는 비효율적이라서
    // xrpl 라이브러릴 사용하여 코드상에서 서명을 한다.
    for (const grant of grants) {
      const prepared = await this.xrpl.autofill({
        TransactionType: "Payment",
        acount: pay.address,
        amount: xrpl.xrpToDrops(),
        destination: grant.owner,
      });
      const signed = wallet.sign(prepared);
      const result = await this.xrpl.submitAndWait(signed.tx_blob);
      return result;
    }
  }
  async EscrowFinish(donationInfosByProductId, data, res) {
    await this.xrpl.connect();

    // grant 제안자가 기부금을 받았다는 것을 AccountSet 트랜잭션으로 날리기
    try {
      await this.xumm.payload
        .createAndSubscribe(
          {
            txjson: {
              TransactionType: "AccountSet",
              Account: data.address,
              Memos: [
                {
                  Memo: {
                    MemoType: Buffer.from("getDonation")
                      .toString("hex")
                      .toUpperCase(),
                    MemoData: Buffer.from("Owner can take donations").toString(
                      "hex"
                    ),
                  },
                },
              ],
            },
            options: {
              return_url: {
                web: "http://localhost:5137",
              },
            },
          },
          (eventMessage) => {
            if (Object.keys(eventMessage.data).indexOf("opened") > -1) {
              // Update the UI? The payload was opened.
            }
            if (Object.keys(eventMessage.data).indexOf("signed") > -1) {
              // The `signed` property is present, true (signed) / false (rejected)
              return eventMessage;
            }
          }
        )
        .then(({ created, resolved }) => {
          console.log("Payload URL:", created.next.always);
          console.log("Payload QR:", created.refs.qr_png);
          res.send(created.next.always);
          return resolved; // Return payload promise for the next `then`
        })
        .then((payload) => {
          console.log("Payload resolved", payload);
        });
    } catch (error) {
      console.log(error);
    }

    // 실제로 돈을 받기 위해 EscrowFinish 트랜잭션을 만들고 지갑으로 서명을 안하고 코드상에서 한번에 처리
    for (const donation of donationInfosByProductId) {
      const prepared = await this.xrpl.autofill({
        TransactionType: "EscrowFinish",
        Account: data.address,
        Owner: donation.owner,
        OfferSequence: donation.sequence,
      });
      const signed = wallet.sign(prepared);
      const result = await this.xrpl.submitAndWait(signed.tx_blob);
      return result;
    }
  }
}

module.exports = XRPLService;
