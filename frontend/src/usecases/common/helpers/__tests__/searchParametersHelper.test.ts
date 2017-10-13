import {Status} from '../../../../types/Types';
import {SearchParameter} from '../../../search/models/searchParameterModels';
import {searchParametersOf} from '../searchParametersHelper';

describe('searchParametersHelper', () => {

  it('creates list of search parameters', () => {
    const searchParameters: SearchParameter[] = [
      {
        name: 'area',
        value: 'Göteborg',
      },
      {
        name: 'area',
        value: 'Kungsbacka',
      },
    ];
    expect(searchParametersOf('area', ['Göteborg', 'Kungsbacka'])).toEqual(searchParameters);
  });

  it('creates list of search parameters for status types', () => {
    const searchParameters: SearchParameter[] = [
      {
        name: 'status',
        value: Status.warning,
      },
      {
        name: 'status',
        value: Status.ok,
      },
    ];
    expect(searchParametersOf('status', [Status.warning, Status.ok])).toEqual(searchParameters);
  });
});
