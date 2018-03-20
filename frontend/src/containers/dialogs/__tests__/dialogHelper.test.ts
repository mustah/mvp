import {initLanguage} from '../../../i18n/i18n';
import {Gateway} from '../../../state/domain-models-paginated/gateway/gatewayModels';
import {normalizedStatusChangelogFor, titleOf} from '../dialogHelper';

describe('dialogSelectors', () => {

  describe('titleOf', () => {

    beforeEach(() => {
      initLanguage({code: 'en', name: 'english'});
    });

    it('prints out default message when no flags are available', () => {
      expect(titleOf([])).toEqual('no');
    });

    it('renders flags with one item', () => {
      expect(titleOf([{title: 'has error'}])).toEqual('has error');
    });

    it('renders all flags and joins them', () => {
      const flags = [{title: 'has error'}, {title: 'ok'}, {title: 'fixed'}];

      expect(titleOf(flags)).toEqual('has error, ok, fixed');
    });

  });

  describe('normalized status changelogs for a given gateway', () => {

    const gateway: Gateway = {
      id: '12032010',
      serial: '005',
      location: {
        address: {id: 'Stockholmsv 33', name: 'Stockholmsv 33'},
        city: {id: 'Perstorp', name: 'Perstorp'},
        position: {longitude: 14.205929, latitude: 59.666749, confidence: 0.6666666666666666},
      },
      flags: [],
      flagged: false,
      productModel: 'CMi2110',
      status: {name: 'OK', id: 0},
      statusChangelog: [{
        date: '2017-11-22 09:34',
        status: {id: 0, name: 'OK'},
        id: '967af275-0026-43b9-a0ef-123dfb05612a',
        gatewayId: '12032010',
      }, {
        date: '2017-11-22 10:34',
        status: {id: 0, name: 'OK'},
        id: '6e4daf1f-a611-42e4-8ebf-ed9e10a7b4fb',
        gatewayId: '12032010',
      }, {
        date: '2017-11-22 11:34',
        status: {id: 3, name: 'Fel'},
        id: 'ac359487-0e7b-4ed2-85bb-d0f75e9d7a27',
        gatewayId: '12032010',
      }, {
        date: '2017-11-22 12:34',
        status: {id: 0, name: 'OK'},
        id: '3e4a4295-2d1a-4118-b303-16fbb3ddfa49',
        gatewayId: '12032010',
      }],
      statusChanged: '2017-11-05 23:00',
      meterIds: ['67606228'],
    };

    it('normalizes and uses only statusChangelog property', () => {
      expect(normalizedStatusChangelogFor(gateway)).toEqual({
        entities:
          {
            '967af275-0026-43b9-a0ef-123dfb05612a':
              {
                date: '2017-11-22 09:34',
                status: {id: 0, name: 'OK'},
                id: '967af275-0026-43b9-a0ef-123dfb05612a',
                gatewayId: '12032010',
              },
            '6e4daf1f-a611-42e4-8ebf-ed9e10a7b4fb':
              {
                date: '2017-11-22 10:34',
                status: {id: 0, name: 'OK'},
                id: '6e4daf1f-a611-42e4-8ebf-ed9e10a7b4fb',
                gatewayId: '12032010',
              },
            'ac359487-0e7b-4ed2-85bb-d0f75e9d7a27':
              {
                date: '2017-11-22 11:34',
                status: {id: 3, name: 'Fel'},
                id: 'ac359487-0e7b-4ed2-85bb-d0f75e9d7a27',
                gatewayId: '12032010',
              },
            '3e4a4295-2d1a-4118-b303-16fbb3ddfa49':
              {
                date: '2017-11-22 12:34',
                status: {id: 0, name: 'OK'},
                id: '3e4a4295-2d1a-4118-b303-16fbb3ddfa49',
                gatewayId: '12032010',
              },
          },
        result: [
          '967af275-0026-43b9-a0ef-123dfb05612a',
          '6e4daf1f-a611-42e4-8ebf-ed9e10a7b4fb',
          'ac359487-0e7b-4ed2-85bb-d0f75e9d7a27',
          '3e4a4295-2d1a-4118-b303-16fbb3ddfa49',
        ],
      });
    });
  });
});
