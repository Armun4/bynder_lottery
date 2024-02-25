package com.bynder.lottery.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bynder.lottery.domain.Ballot;
import com.bynder.lottery.domain.Lottery;
import com.bynder.lottery.domain.Participant;
import com.bynder.lottery.repository.BallotRepository;
import com.bynder.lottery.repository.LotteryRepository;
import com.bynder.lottery.repository.ParticipantRepository;
import com.bynder.lottery.repository.WinnerBallotRepository;
import com.bynder.lottery.util.BallotArbitrarityProvider;
import com.bynder.lottery.util.LotteryArbitraryProvider;
import com.bynder.lottery.util.ParticipantArbitraryProvider;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;
import net.jqwik.api.Arbitraries;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BallotServiceTest {

  @Mock BallotRepository ballotRepository;
  @Mock ParticipantRepository participantRepository;

  @Mock LotteryRepository lotteryRepository;

  @Mock WinnerBallotRepository winnerBallotRepository;

  ArgumentCaptor<List<Ballot>> argumentCaptor = ArgumentCaptor.forClass(List.class);

  @InjectMocks BallotService ballotService;

  @Test
  void canSave() {

    Participant participant = ParticipantArbitraryProvider.arbitraryParticipants().sample();
    Mockito.when(participantRepository.get(participant.getId()))
        .thenReturn(Optional.of(participant));

    Lottery lottery = LotteryArbitraryProvider.arbitraryLottery().sample();

    Mockito.when(lotteryRepository.getCurrentLottery()).thenReturn(Optional.of(lottery));

    int amount = 3;
    ballotService.saveBallots(participant.getId(), amount);

    List<Ballot> expected =
        Stream.generate(
                () ->
                    Ballot.builder()
                        .lotteryId(lottery.getId())
                        .participantId(participant.getId())
                        .build())
            .limit(amount)
            .toList();

    verify(participantRepository).get(participant.getId());
    verify(lotteryRepository).getCurrentLottery();
    verify(ballotRepository).saveAll(argumentCaptor.capture());

    List<Ballot> capturedArguments = argumentCaptor.getValue();

    assertThat(capturedArguments).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  void shouldThrowExceptionNoParticipant() {

    long participantId = Arbitraries.longs().sample();
    Mockito.when(participantRepository.get(participantId)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> {
              ballotService.saveBallots(participantId, 1);
            })
        // Specify the expected exception type
        .isInstanceOf(NoSuchElementException.class)
        // Optionally, assert the message or other details of the exception
        .hasMessage("Participant not found, please register");
  }

  @Test
  void shouldThrowExceptionNoLottery() {

    Participant participant = ParticipantArbitraryProvider.arbitraryParticipants().sample();
    Mockito.when(participantRepository.get(participant.getId()))
        .thenReturn(Optional.of(participant));

    assertThatThrownBy(
            () -> {
              ballotService.saveBallots(participant.getId(), 1);
            })
        // Specify the expected exception type
        .isInstanceOf(RuntimeException.class)
        // Optionally, assert the message or other details of the exception
        .hasMessage("No current lottery found. Please check again later.");
  }

  @Test
  void canGetBallotsForLotteryId() {

    long lotteryId = Arbitraries.longs().sample();

    List<Ballot> ballots =
        BallotArbitrarityProvider.arbitraryBallotsForLottery(lotteryId)
            .list()
            .ofMaxSize(12)
            .sample();
    when(ballotRepository.findAllByLotteryId(lotteryId)).thenReturn(ballots);

    List<Ballot> result = ballotService.getAllBallotsForLottery(lotteryId);

    verify(ballotRepository, Mockito.times(1)).findAllByLotteryId(lotteryId);
  }
}