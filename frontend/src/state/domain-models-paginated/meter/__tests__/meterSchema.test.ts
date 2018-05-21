import {meterProcessStrategy} from '../meterSchema';

describe('meterSchema', () => {

  describe('meterProcessStrategy ', () => {

    it('normalizes null read interval to undefined', () => {
      expect(meterProcessStrategy({readIntervalMinutes: null})).toEqual({});
    });

    it('doesn not touch non-null read interval', () => {
      expect(meterProcessStrategy({readIntervalMinutes: 19})).toEqual({readIntervalMinutes: 19});
    });

  });
});
