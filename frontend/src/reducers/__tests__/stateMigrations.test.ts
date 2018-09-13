import {migrateFromUuidToIdNamed, migrateUserSelection} from '../stateMigrations';

describe('rootReducer', () => {

  describe('migrateFromUuidToIdNamed', () => {

    it('handles empty selection parameters', () => {
      expect(migrateFromUuidToIdNamed({})).toEqual({
        facilities: [],
        gatewaySerials: [],
        media: [],
        meterStatuses: [],
        secondaryAddresses: [],
      });
    });

    it('maps facilities', () => {
      expect(migrateFromUuidToIdNamed({facilities: [1]})).toEqual({
        facilities: [{id: 1, name: 1}],
        gatewaySerials: [],
        media: [],
        meterStatuses: [],
        secondaryAddresses: [],
      });
    });

    it('maps all', () => {
      const actual = migrateFromUuidToIdNamed({
        facilities: [1],
        gatewaySerials: [2, 3],
        media: ['Gas'],
        meterStatuses: ['critical'],
        secondaryAddresses: ['000123123'],
      });

      expect(actual).toEqual({
        facilities: [{id: 1, name: 1}],
        gatewaySerials: [{id: 2, name: 2}, {id: 3, name: 3}],
        media: [{id: 'Gas', name: 'Gas'}],
        meterStatuses: [{id: 'critical', name: 'critical'}],
        secondaryAddresses: [{id: '000123123', name: '000123123'}],
      });
    });

  });

  describe('migrateUserSelection', () => {

    const oldUserSelection = {
      id: '1',
      ownerUserId: '333',
      name: 'all',
      selectionParameters: {
        media: [],
        alarms: [],
        cities: [],
        addresses: [],
        dateRange: {period: 'latest'},
        manufacturers: [],
        meterStatuses: [],
        productModels: [],
      },
      organisationId: 'org1',
      isChanged: false,
    };

    it('migrates from old empty user selection state to new state', () => {
      const expected = {
        ...oldUserSelection,
        selectionParameters: {
          ...oldUserSelection.selectionParameters,
          facilities: [],
          gatewaySerials: [],
          secondaryAddresses: [],
        },
      };

      expect(migrateUserSelection(oldUserSelection)).toEqual(expected);
    });

    it('migrates old media ids to new id named version', () => {
      const selection = {
        ...oldUserSelection,
        selectionParameters: {
          ...oldUserSelection.selectionParameters,
          media: ['Gas', 'Water'],
        },
      };
      const expected = {
        ...oldUserSelection,
        selectionParameters: {
          ...oldUserSelection.selectionParameters,
          media: [{id: 'Gas', name: 'Gas'}, {id: 'Water', name: 'Water'}],
          facilities: [],
          gatewaySerials: [],
          secondaryAddresses: [],
        },
      };
      expect(migrateUserSelection(selection)).toEqual(expected);
    });

    it('migrates old city ids to new city objects', () => {
      const selection = {
        ...oldUserSelection,
        selectionParameters: {
          ...oldUserSelection.selectionParameters,
          cities: ['sverige,kungsbacka', 'norge,oslo'],
        },
      };
      const expected = {
        ...oldUserSelection,
        selectionParameters: {
          ...oldUserSelection.selectionParameters,
          cities: [
            {
              id: 'sverige,kungsbacka',
              name: 'kungsbacka',
              country: {id: 'sverige', name: 'sverige'},
            },
            {
              id: 'norge,oslo',
              name: 'oslo',
              country: {id: 'norge', name: 'norge'},
            },
          ],
          facilities: [],
          gatewaySerials: [],
          secondaryAddresses: [],
        },
      };
      expect(migrateUserSelection(selection)).toEqual(expected);
    });

    it('migrates old address ids to new address objects', () => {
      const selection = {
        ...oldUserSelection,
        selectionParameters: {
          ...oldUserSelection.selectionParameters,
          addresses: ['sverige,kungsbacka,kabelgatan 1', 'norge,oslo,kungsgatan 4'],
        },
      };
      const expected = {
        ...oldUserSelection,
        selectionParameters: {
          ...oldUserSelection.selectionParameters,
          addresses: [
            {
              id: 'sverige,kungsbacka,kabelgatan 1',
              name: 'kabelgatan 1',
              city: {id: 'kungsbacka', name: 'kungsbacka'},
              country: {id: 'sverige', name: 'sverige'},
            },
            {
              id: 'norge,oslo,kungsgatan 4',
              name: 'kungsgatan 4',
              city: {id: 'oslo', name: 'oslo'},
              country: {id: 'norge', name: 'norge'},
            },
          ],
          facilities: [],
          gatewaySerials: [],
          secondaryAddresses: [],
        },
      };

      expect(migrateUserSelection(selection)).toEqual(expected);
    });
  });

});
