import {Meter, MetersState} from '../meterModels';
import {DomainModel} from '../../domainModels';
import {uuid} from '../../../../types/Types';
import {getMeterDataSummary} from '../meterSelectors';

type PartialDomainModel = DomainModel<Partial<Meter>>;
describe('meterSelectors', () => {

  it('can summarize a list of meters into groups with a tally each (like, 5 in NY, 3 in LA)', () => {
    const meters: uuid[] = [1, 2, 3];
    const metersLookup: PartialDomainModel = {
      1: {
        flagged: false,
        city: {id: 'sto', name: 'stockholm'},
        manufacturer: 'ELV',
        medium: 'water',
        status: {id: 0, name: 'ok'},
        alarm: 'none',
      },
      2: {
        flagged: false,
        city: {id: 'sto', name: 'stockholm'},
        manufacturer: 'ELV',
        medium: 'air',
        status: {id: 0, name: 'ok'},
        alarm: 'none',
      },
      3: {
        flagged: true,
        city: {id: 'got', name: 'göteborg'},
        manufacturer: 'ELV',
        medium: 'air',
        status: {id: 0, name: 'ok'},
        alarm: 'none',
      },
    };

    const metersState: Partial<MetersState> = {
      entities: metersLookup as DomainModel<Meter>,
      result: meters,
    };

    const reduced = getMeterDataSummary(metersState as MetersState);

    expect(reduced).toEqual({
      flagged:
        {
          flagged: {name: 'flagged', value: 1, filterParam: true},
          unFlagged: {name: 'unFlagged', value: 2, filterParam: false},
        },
      city:
        {
          sto: {name: 'stockholm', value: 2, filterParam: 'sto'},
          got: {name: 'göteborg', value: 1, filterParam: 'got'},
        },
      manufacturer: {
        ELV: {name: 'ELV', value: 3, filterParam: 'ELV'},
      },
      medium:
        {
          water: {name: 'water', value: 1, filterParam: 'water'},
          air: {name: 'air', value: 2, filterParam: 'air'},
        },
      status: {
        0: {name: 'ok', value: 3, filterParam: 0},
      },
      alarm: {
        none: {name: 'none', value: 3, filterParam: 'none'},
      },
    });
  });

});
