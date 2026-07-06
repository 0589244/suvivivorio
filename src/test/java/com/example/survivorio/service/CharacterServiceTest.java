package com.example.survivorio.service;

import com.example.survivorio.entity.AppUser;
import com.example.survivorio.entity.Character;
import com.example.survivorio.repository.CharacterRepository;
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
class CharacterServiceTest {
    @Mock
    private CharacterRepository repository;

    private CharacterService characterService;

    @BeforeEach
    void setUp() {
        characterService = new CharacterService(repository);
    }

    @Test
    void createAssignsOwnerAndCreatedAt() {
        AppUser owner = new AppUser();
        Character character = new Character();
        character.setId(99L);
        character.setName("Aelar");

        when(repository.save(any(Character.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Character saved = characterService.create(owner, character);

        assertThat(saved.getId()).isNull();
        assertThat(saved.getOwner()).isSameAs(owner);
        assertThat(saved.getCreatedAt()).isNotBlank();
        verify(repository).save(character);
    }

    @Test
    void updateOnlyUsesSheetOwnedByCurrentUser() {
        AppUser owner = new AppUser();
        Character existing = new Character();
        existing.setId(7L);
        existing.setOwner(owner);
        existing.setCreatedAt("old-date");

        Character data = new Character();
        data.setName("Mira");
        data.setClassName("Wizard");
        data.setLevel(3);
        data.setHp(18);
        data.setAc(12);
        data.setGear("Spellbook");
        data.setProfileImage("data:image/png;base64,portrait");
        data.setCreatedAt("new-date");

        when(repository.findByIdAndOwner(7L, owner)).thenReturn(Optional.of(existing));
        when(repository.save(any(Character.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Character updated = characterService.update(owner, 7L, data);

        assertThat(updated.getName()).isEqualTo("Mira");
        assertThat(updated.getClassName()).isEqualTo("Wizard");
        assertThat(updated.getLevel()).isEqualTo(3);
        assertThat(updated.getHp()).isEqualTo(18);
        assertThat(updated.getAc()).isEqualTo(12);
        assertThat(updated.getGear()).isEqualTo("Spellbook");
        assertThat(updated.getProfileImage()).isEqualTo("data:image/png;base64,portrait");
        assertThat(updated.getOwner()).isSameAs(owner);
        assertThat(updated.getCreatedAt()).isEqualTo("new-date");
        verify(repository).findByIdAndOwner(7L, owner);
    }

    @Test
    void updateReturnsNotFoundWhenSheetDoesNotBelongToUser() {
        AppUser owner = new AppUser();
        when(repository.findByIdAndOwner(7L, owner)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> characterService.update(owner, 7L, new Character()))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteOnlyRemovesSheetOwnedByCurrentUser() {
        AppUser owner = new AppUser();
        Character existing = new Character();
        existing.setId(7L);
        existing.setOwner(owner);

        when(repository.findByIdAndOwner(7L, owner)).thenReturn(Optional.of(existing));

        characterService.delete(owner, 7L);

        verify(repository).delete(existing);
    }

    @Test
    void deleteReturnsNotFoundWhenSheetDoesNotBelongToUser() {
        AppUser owner = new AppUser();
        when(repository.findByIdAndOwner(7L, owner)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> characterService.delete(owner, 7L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
