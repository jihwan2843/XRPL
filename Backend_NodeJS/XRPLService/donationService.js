const { default: axios } = require("axios");

class DonationService {
  async getDonationInfoByproductId(productId) {
    await axios({
      method: "GET",
      url: `http://158.179.169.106:8081/api/v1/product/donate?productId=${productId}`,
      headers: {
        "Content-Type": "application/json",
        Authorization: token,
      },
    })
      .then((response) => {
        console.log(response);
        return response.data;
      })
      .catch((error) => {
        console.log(error);
      });
  }
}

module.exports = DonationService;
