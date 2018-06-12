import {statusClassName} from '../thresholds';

describe('thresholds', () => {

  describe('statusClassName', () => {
    test('given -1 return "error" css', () => {
      expect(statusClassName(-1)).toEqual('error');
    });

    test('given 69 return "error" css', () => {
      expect(statusClassName(69)).toEqual('error');
    });

    test('given 70 return "error" css', () => {
      expect(statusClassName(70)).toEqual('error');
    });

    test('given 71 return "warning" css', () => {
      expect(statusClassName(71)).toEqual('warning');
    });

    test('given 99 return "warning" css', () => {
      expect(statusClassName(99)).toEqual('warning');
    });

    test('given 100 return "ok" css', () => {
      expect(statusClassName(100)).toEqual('ok');
    });

    test('given 101 return "ok" css', () => {
      expect(statusClassName(101)).toEqual('ok');
    });
  });
});
