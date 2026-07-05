package com.example.survivorio.service;

import com.example.survivorio.entity.AppUser;
import com.example.survivorio.entity.Monster;
import com.example.survivorio.repository.MonsterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonsterServiceTest {
    @Mock
    private MonsterRepository repository;

    private MonsterService monsterService;

    @BeforeEach
    void setUp() {
        monsterService = new MonsterService(repository);
    }

    @Test
    void createAssignsOwnerAndCreatedAt() {
        AppUser owner = new AppUser();
        Monster monster = new Monster();
        monster.setId(42L);
        monster.setName("Bandit Captain");

        when(repository.save(any(Monster.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Monster saved = monsterService.create(owner, monster);

        assertThat(saved.getId()).isNull();
        assertThat(saved.getOwner()).isSameAs(owner);
        assertThat(saved.getCreatedAt()).isNotBlank();
        verify(repository).save(monster);
    }

    @Test
    void updateOnlyUsesMonsterOwnedByCurrentUser() {
        AppUser owner = new AppUser();
        Monster existing = new Monster();
        existing.setId(5L);
        existing.setOwner(owner);

        Monster data = new Monster();
        data.setName("Knight Captain");
        data.setType("Humanoid");
        data.setArmorClass(16);
        data.setHitPoints(35);
        data.setChallenge("2");
        data.setGear("Longsword");

        when(repository.findByIdAndOwner(5L, owner)).thenReturn(Optional.of(existing));
        when(repository.save(any(Monster.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Monster updated = monsterService.update(owner, 5L, data);

        assertThat(updated.getName()).isEqualTo("Knight Captain");
        assertThat(updated.getType()).isEqualTo("Humanoid");
        assertThat(updated.getArmorClass()).isEqualTo(16);
        assertThat(updated.getHitPoints()).isEqualTo(35);
        assertThat(updated.getChallenge()).isEqualTo("2");
        assertThat(updated.getGear()).isEqualTo("Longsword");
        assertThat(updated.getOwner()).isSameAs(owner);
        verify(repository).findByIdAndOwner(5L, owner);
    }

    @Test
    void updateReturnsNotFoundWhenMonsterDoesNotBelongToUser() {
        AppUser owner = new AppUser();
        when(repository.findByIdAndOwner(5L, owner)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> monsterService.update(owner, 5L, new Monster()))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteOnlyRemovesMonsterOwnedByCurrentUser() {
        AppUser owner = new AppUser();
        Monster existing = new Monster();
        existing.setId(5L);
        existing.setOwner(owner);

        when(repository.findByIdAndOwner(5L, owner)).thenReturn(Optional.of(existing));

        monsterService.delete(owner, 5L);

        verify(repository).delete(existing);
    }

    @Test
    void deleteReturnsNotFoundWhenMonsterDoesNotBelongToUser() {
        AppUser owner = new AppUser();
        when(repository.findByIdAndOwner(5L, owner)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> monsterService.delete(owner, 5L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
