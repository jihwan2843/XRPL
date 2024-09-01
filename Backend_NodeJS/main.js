const XRPLService = require("./XRPLService/xrplService");
const GrantService = require("./XRPLService/grantService");
const { Xumm } = require("xumm");
const express = require("express");
const env = require("dotenv");
const { default: axios } = require("axios");
const QuadraticFundingService = require("./XRPLService/quadraticFundingService");
const DonationService = require("./XRPLService/donationService");
const app = express();

// JSON 파싱 미들웨어 추가
app.use(express.json());
env.config();

const xumm = new Xumm(process.env.XUMM_API_KEY, process.env.XUMM_API_SECRET);

const xrpl = new XRPLService();
const grantService = new GrantService();
const quadraticService = new QuadraticFundingService();
const donationService = new DonationService();

xumm.on("ready", () => console.log("Ready"));

let account;

app.post("/connectWallet", async (req, res) => {
  const result = await xrpl.SignIn(res);
  account = result;
});
// QR 코드를 response로 전송을 이미 하였기 때문에 /getAccount로
// API 요청을 한 번 더하여 account를 얻는다.
app.get("/getAccount", (req, res) => {
  res.send(account);
  account = null;
});

app.post("/AccountSet", async (req, res) => {
  // 프론트에서 받은 데이터
  const grant = req.body;
  // AccountSet 트랜잭션 작동
  xrpl.AccountSet(grant, res);
  //Spring 서버에 데이터 보내기
  grantService.createGrant(grant);
});

app.post("/escrowCreate", async (req, res) => {
  // 프론트에서 받은 데이터
  const donation = req.body;
  // EscrowFinish 트랜잭션을 하기 위해 sequence 값을 저장
  const sequence = await xrpl.EscrowCreate(donation, res);
  //Spring 서버에 데이터 보내기
  grantService.donateGrant(sequence, donation);
});

app.post("/Payment", async (req, res) => {
  // 프론트에서 받은 데이터
  const pay = req.body;

  // Spring DB에서 GrantInfo의 모든 데이터들을 가지고 온다
  const grantInfos = grantService.getAllGrantInfo();

  // ProductId 리스트를 만든다.
  const productIds = grantService.getProductIdList(grantInfos);

  // productId : 퍼센트(실수형으로 저장 되어 있음)
  // Quadratic Fudning 방식의 분배를 위한 계산
  const fundingRate = quadraticService.calculateDistributionRate(productIds);

  let totalMatchingPoolAmount;

  const grants = QuadraticFundingService.distributeFunding(
    totalMatchingPoolAmount,
    fundingRate,
    grantInfos
  );

  xrpl.Payment(grants, pay, res);
});

app.post("/getDonation", async (req, res) => {
  // 프론트에서 받은 데이터
  const data = req.body;

  // Spring DB에서 특정 productId인 모든 DonationInfo 데이터들을 가지고 온다.
  const donationInfosByProductId = donationService.getDonationInfoByproductId(
    get.productId
  );
  xrpl.EscrowFinish(donationInfosByProductId, data, res);
});

app.listen(3000);
