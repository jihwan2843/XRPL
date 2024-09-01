const { default: axios } = require("axios");

class GrantService {
  constructor() {}

  async createGrant(grant) {
    await axios({
      method: "Post",
      url: "http://158.179.169.106:8081/api/v1/product/create",
      data: {
        productId: grant.productId,
        amount: grant.amount,
      },
      headers: {
        "Content-Type": "application/json",
        Authorization: token,
      },
    })
      .then((response) => {
        console.log(response);
      })
      .catch((error) => {
        console.log(error);
      });
  }

  async donateGrant(sequence, donation) {
    await axios({
      method: "post",
      url: "http://158.179.169.106:8081/api/v1/product/donate",
      data: {
        amount: donation.amount,
        productId: donation.productId,
        sequence: sequence,
      },
      headers: {
        "Content-Type": "application/json",
        Authorization: token,
      },
    })
      .then((response) => console.log(response))
      .catch((error) => console.log(error));
  }

  async getAllGrantInfo() {
    await axios({
      method: "Get",
      headers: {
        "Content-Type": "application/json",
        Authorization: token,
      },
    })
      .then((response) => {
        console.log(response);
        return response;
      })
      .catch((error) => {
        console.log(error);
      });
  }

  getProductIdList(grantInfos) {
    const grantIds = [];

    for (const grant of grantInfos) {
      // Grant의 상태가 'COMPLETED'일 때 자금을 분배 가능
      if (grant.status === "COMPLETED") {
        grantIds.push(grant.productId);
      } else {
        // 상태가 'COMPLETED'가 아닐 경우 에러 발생
        throw new Error(
          `Grant with ID ${grant.productId} is not in COMPLETED status. Status: ${grant.productId}`
        );
      }
    }
    return grantIds;
  }
}

module.exports = GrantService;
