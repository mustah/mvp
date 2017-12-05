import {DomainModel} from '../../../../state/domain-models/domainModels';
import {Meter} from '../../../../state/domain-models/meter/meterModels';
import {uuid} from '../../../../types/Types';
import {dataSummary} from '../overviewHelper';

type PartialDomainModel = DomainModel<Partial<Meter>>;
describe('inc', () => {

  it('counts', () => {

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

    const reduced = dataSummary(meters, metersLookup as DomainModel<Meter>);

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
