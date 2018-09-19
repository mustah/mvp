import {thresholdClassName} from '../thresholds';

describe('thresholds', () => {

  describe('thresholdClassName', () => {
    test('given -1 return "error" css', () => {
      expect(thresholdClassName(-1)).toEqual('error');
    });

    test('given 69 return "error" css', () => {
      expect(thresholdClassName(69)).toEqual('error');
    });

    test('given 70 return "error" css', () => {
      expect(thresholdClassName(70)).toEqual('error');
    });

    test('given 71 return "warning" css', () => {
      expect(thresholdClassName(71)).toEqual('warning');
    });

    test('given 99 return "warning" css', () => {
      expect(thresholdClassName(99)).toEqual('warning');
    });

    test('given 100 return "ok" css', () => {
      expect(thresholdClassName(100)).toEqual('ok');
    });

    test('given 101 return "ok" css', () => {
      expect(thresholdClassName(101)).toEqual('ok');
    });
  });
});
