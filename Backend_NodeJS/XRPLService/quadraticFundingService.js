const DonationService = require("./donationService");

class QuadraticFundingService {
  constructor() {
    this.donationService = new DonationService();
  }
  async calculateSumOfSqrt(productId) {
    // 특정 grant 후원 금액의 제곱근의 합
    let sum = 0.0;

    // DB에서 특정 grantId에 해당하는 DonationInfo 전체 데이터를 가져온다.
    const donationInfos =
      this.donationService.getDonationInfoByproductId(productId);

    // Quadratic Funding 계산 공식
    // 각 그랜트에 각 사용자가 기부한 금액의 제곱들의 합의 제곱을 구한다.
    for (const donation of donationInfos) {
      sum += Math.sqrt(donation.amount);
    }

    return sum * sum;
  }

  calculateDistributionRate(productIds) {
    // productId : rate
    const fundingRate = {};

    let sum = 0.0;

    // 제곱근들의 합의 제곱을 productId별로 매핑시켜 저장한다.
    for (const productId of productIds) {
      const rate = calculateSumOfSqrt(productId);
      sum += rate;
      fundingRate[productId] = rate;
    }

    // 분배 비율(실수형)을 구한다.
    for (const productId of productIds) {
      const rate = fundingRate[productId] / sum;
      fundingRate[grantId] = rate;
    }

    return fundingRate;
  }

  // 계산된 분배 비율로 각 그랜트의 분배 금액을 저장한다.
  distributeFunding(totalMatchingPoolAmount, fundingRate, grants) {
    for (const grant of grants) {
      const rate = fundingRate[grant.productId];
      grant.matchingPoolAmount = Math.floor(totalMatchingPoolAmount * rate);
    }
    return grants;
  }
}

module.exports = QuadraticFundingService;
