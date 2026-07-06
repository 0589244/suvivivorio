import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

import SheetList from './SheetList.vue'
import type { SheetRecord } from '../types/sheets'

const entries: SheetRecord[] = [
  {
    id: 1,
    sheetType: 'character',
    name: 'Aelar',
    ancestry: 'Elf',
    className: 'Ranger',
    level: 2,
    xp: 100,
    str: 10,
    dex: 16,
    con: 12,
    intel: 11,
    wis: 14,
    cha: 9,
    hp: 18,
    ac: 14,
    title: 'Scout',
    alignment: 'Neutral',
    background: 'Outlander',
    deity: '',
    talentsSpells: 'Tracking',
    attacks: 'Bow',
    gear: 'Rope',
    profileImage: 'data:image/png;base64,portrait',
    gp: 10,
    sp: 2,
    cp: 0,
    createdAt: '2026-07-05T10:00:00Z',
  },
  {
    id: 2,
    sheetType: 'monster',
    name: 'Bandit Captain',
    type: 'Humanoid',
    armorClass: 15,
    hitPoints: 21,
    challenge: '1',
    notes: 'Leader of the hideout',
    str: 8,
    dex: 14,
    con: 10,
    intel: 10,
    wis: 8,
    cha: 10,
    attacks: 'Scimitar',
    gear: 'Shield',
    profileImage: '',
    gp: 5,
    sp: 8,
    cp: 12,
    createdAt: '2026-07-05T11:00:00Z',
  },
]

describe('SheetList', () => {
  it('renders all sheets and emits the selected entry', async () => {
    const wrapper = mount(SheetList, {
      props: {
        entries,
        isLoading: false,
        error: null,
      },
    })

    expect(wrapper.text()).toContain('Aelar')
    expect(wrapper.text()).toContain('Bandit Captain')
    const firstEntry = entries[0]
    if (!firstEntry) {
      throw new Error('Expected a first sheet entry')
    }
    expect(wrapper.find('.sheet-avatar-image').attributes('src')).toBe(firstEntry.profileImage)
    const cards = wrapper.findAll('.sheet-card')
    expect(cards).toHaveLength(2)

    const secondCard = cards[1]
    if (!secondCard) {
      throw new Error('Expected a second sheet card')
    }

    await secondCard.trigger('click')

    expect(wrapper.emitted('select')).toEqual([[entries[1]]])
  })

  it('shows an empty state when no sheets exist yet', () => {
    const wrapper = mount(SheetList, {
      props: {
        entries: [],
        isLoading: false,
        error: null,
      },
    })

    expect(wrapper.text()).toContain('Noch keine Eintraege')
  })
})
