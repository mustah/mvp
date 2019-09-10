import {BatchRequestState} from '../../../../../state/domain-models-paginated/batch-references/batchReferenceModels';
import {logoutUser} from '../../../../auth/authActions';
import {changeRequireApproval, changeBatchReference, selectDeviceEuis} from '../batchReferenceActions';
import {batchReferenceReducer, initialState} from '../batchReferenceReducer';

describe('batch reference reducer', () => {

  describe('has initial state for un handled action type', () => {
    expect(batchReferenceReducer(initialState, logoutUser(undefined))).toBe(initialState);
  });

  describe('batch id', () => {

    it('can be added', () => {
      const state = batchReferenceReducer(initialState, changeBatchReference('1'));

      const expected: BatchRequestState = {...initialState, batchId: '1'};

      expect(state).toEqual(expected);
    });

    it('can be replaces', () => {
      const state: BatchRequestState = {...initialState, batchId: '123'};

      const nextState = batchReferenceReducer(state, changeBatchReference('abc'));

      const expected: BatchRequestState = {...initialState, batchId: 'abc'};

      expect(nextState).toEqual(expected);
    });

    it('can clear with empty string', () => {
      const state: BatchRequestState = {...initialState, batchId: '123'};

      const nextState = batchReferenceReducer(state, changeBatchReference(''));

      expect(nextState).toEqual(initialState);
      expect(nextState).not.toBe(initialState);
    });

  });

  describe('change require approval', () => {

    it('requires approval ', () => {
      const state = batchReferenceReducer(initialState, changeRequireApproval(true));

      const expected: BatchRequestState = {...initialState, requireApproval: true};
      expect(state).toEqual(expected);
    });

    it('toggles approval', () => {
      const state: BatchRequestState = {...initialState, requireApproval: true};

      const nextState = batchReferenceReducer(state, changeRequireApproval(false));

      const expected: BatchRequestState = {...initialState, requireApproval: false};
      expect(nextState).toEqual(expected);
    });

  });

  describe('select device euis', () => {

    it('can add single device id', () => {
      const state = batchReferenceReducer(initialState, selectDeviceEuis(['a']));

      const expected: BatchRequestState = {...initialState, deviceEuis: ['a']};
      expect(state).toEqual(expected);
    });

    it('can add several device ids', () => {
      const state = batchReferenceReducer(initialState, selectDeviceEuis(['a', 'b', '1']));

      const expected: BatchRequestState = {...initialState, deviceEuis: ['a', 'b', '1']};
      expect(state).toEqual(expected);
    });

    it('clear all device ids', () => {
      const state: BatchRequestState = {...initialState, deviceEuis: ['a', 'b', '1']};

      const nextState = batchReferenceReducer(state, selectDeviceEuis([]));

      const expected: BatchRequestState = {...initialState, deviceEuis: []};
      expect(nextState).toEqual(expected);
    });

  });

});
