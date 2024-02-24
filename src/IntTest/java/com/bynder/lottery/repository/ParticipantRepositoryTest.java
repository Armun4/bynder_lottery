package com.bynder.lottery.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.bynder.lottery.BaseIT;
import com.bynder.lottery.domain.Participant;
import com.bynder.lottery.repository.jpa.ParticipantJpaRepository;
import com.bynder.lottery.util.ParticipantArbitraryProvider;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ParticipantRepositoryTest extends BaseIT {

  @Autowired ParticipantJpaRepository participantJpaRepository;

  @Autowired ParticipantRepository participantRepository;

  @BeforeEach
  void setUp() {
    participantJpaRepository.deleteAll();
  }

  @Test
  void IdsAssignedByDb() {

    List<Participant> participants =
        ParticipantArbitraryProvider.arbitraryParticipants().list().ofMaxSize(10).sample();

    participants.forEach(
        participant -> {
          Assertions.assertThat(participant.getId()).isNull();
        });

    List<Participant> saved =
        participants.stream()
            .map(
                participant -> {
                  return participantRepository.save(participant);
                })
            .toList();

    saved.forEach(
        participant -> {
          Assertions.assertThat(participant.getId()).isNotNull();
        });
  }

    @Test
    void CanSaveAndUpdate() {
    Participant participant = ParticipantArbitraryProvider.arbitraryParticipants().sample();

    Participant saved = participantRepository.save(participant);

    Optional<Participant> result = participantRepository.get(saved.getId());

    Assertions.assertThat(result).isPresent();

    Assertions.assertThat(result.get())
        .usingRecursiveComparison()
        .ignoringFields("id")
        .isEqualTo(participant);
  }
}