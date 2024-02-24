package com.bynder.lottery.repository;

import com.bynder.lottery.BaseIT;
import com.bynder.lottery.domain.Ballot;
import com.bynder.lottery.repository.jpa.BallotJpaRepository;
import com.bynder.lottery.util.BallotArbitrarityProvider;
import java.util.List;
import net.jqwik.api.Arbitraries;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class BallotRepositoryIT extends BaseIT {

  @Autowired BallotJpaRepository ballotJpaRepository;
  @Autowired BallotRepository ballotRepository;

  @BeforeEach
  void setUp() {
    ballotJpaRepository.deleteAll();
  }

  @Test
  void IdsAssignedByDb() {

    List<Ballot> ballots = getBallots();

    List<Ballot> result = ballotRepository.saveAll(ballots);

    result.forEach(
        ballot -> {
          Assertions.assertThat(ballot.getId()).isNotNull();
        });
  }

  @Test
  void a() {
    long lotteryId = Arbitraries.longs().sample();

    List<Ballot> ballots = getBallotsForLottery(lotteryId);

    ballotRepository.saveAll(ballots);

    List<Ballot> result = ballotRepository.findAllByLotteryId(lotteryId);

    result.forEach(
        ballot -> {
          Assertions.assertThat(ballot.getLotteryId()).isEqualTo(lotteryId);
        });
  }

  private List<Ballot> getBallots() {
    return BallotArbitrarityProvider.arbitraryBallots().list().ofMaxSize(20).sample();
  }

  private List<Ballot> getBallotsForLottery(long lotteryId) {
    return BallotArbitrarityProvider.arbitraryBallotsForLottery(lotteryId)
        .list()
        .ofMaxSize(20)
        .sample();
  }
}
