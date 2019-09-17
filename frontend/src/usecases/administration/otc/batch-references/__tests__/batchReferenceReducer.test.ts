import {fromCommaSeparated} from '../../../../../helpers/commonHelpers';
import {Maybe} from '../../../../../helpers/Maybe';
import {
  BatchReferencePayload,
  BatchRequestState
} from '../../../../../state/domain-models-paginated/batch-references/batchReferenceModels';
import {logoutUser} from '../../../../auth/authActions';
import {changeBatchReference, changeDeviceEuis, changeRequireApproval} from '../batchReferenceActions';
import {batchReferenceReducer, initialState} from '../batchReferenceReducer';

describe('batch reference reducer', () => {

  describe('has initial state for un handled action type', () => {
    expect(batchReferenceReducer(initialState, logoutUser(undefined))).toBe(initialState);
  });

  describe('batch id', () => {

    describe('without short prefix', () => {

      const shortPrefix = Maybe.nothing<string>();

      it('can be added', () => {
        const state = batchReferenceReducer(initialState, changeBatchReference({shortPrefix, value: '1'}));

        const expected: BatchRequestState = {...initialState, batchId: '1'};

        expect(state).toEqual(expected);
      });

      it('can be replaces', () => {
        const state: BatchRequestState = {...initialState, batchId: '123'};

        const nextState = batchReferenceReducer(state, changeBatchReference({shortPrefix, value: 'abc'}));

        const expected: BatchRequestState = {...initialState, batchId: 'abc'};

        expect(nextState).toEqual(expected);
      });

      it('can clear with empty string', () => {
        const state: BatchRequestState = {...initialState, batchId: '123'};

        const nextState = batchReferenceReducer(state, changeBatchReference({shortPrefix, value: ''}));

        expect(nextState).toEqual(initialState);
        expect(nextState).not.toBe(initialState);
      });
    });

    describe('with short prefix', () => {

      const shortPrefix = Maybe.maybe<string>('evo');

      it('can be added', () => {
        const state = batchReferenceReducer(initialState, changeBatchReference({shortPrefix, value: '1'}));

        const expected: BatchRequestState = {...initialState, batchId: 'evo_1'};

        expect(state).toEqual(expected);
      });

      it('can be clear to just short prefix', () => {
        const state: BatchRequestState = {...initialState, batchId: 'evo_1'};

        const nextState = batchReferenceReducer(state, changeBatchReference({shortPrefix, value: 'evo_12'}));

        const expected: BatchRequestState = {...initialState, batchId: 'evo_12'};

        expect(nextState).toEqual(expected);
      });

      it('can clear the batch id', () => {
        const state: BatchRequestState = {...initialState, batchId: 'evo_1'};

        const nextState = batchReferenceReducer(state, changeBatchReference({shortPrefix, value: 'evo'}));

        const expected: BatchRequestState = {...initialState, batchId: ''};

        expect(nextState).toEqual(expected);
      });

      it('can add short prefix as the value', () => {
        const nextState = batchReferenceReducer(initialState, changeBatchReference({shortPrefix, value: 'evo_evo'}));

        const expected: BatchRequestState = {...initialState, batchId: 'evo_evo'};

        expect(nextState).toEqual(expected);
      });
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

    it('can add several device ids', () => {
      const state = batchReferenceReducer(initialState, changeDeviceEuis('a, b, 1'));

      const expected: BatchRequestState = {...initialState, deviceEuisText: 'a, b, 1'};
      expect(state).toEqual(expected);
    });

    it('clear all device ids', () => {
      const state = batchReferenceReducer({...initialState, deviceEuisText: 'ab, 123'}, changeDeviceEuis(''));

      expect(state).toEqual(initialState);
    });

  });

  describe('comma separated device ids', () => {

    it('handles empty string', () => {
      expect(fromCommaSeparated('')).toEqual([]);
    });

    it('can add several device ids', () => {
      expect(fromCommaSeparated('a, b, 1')).toEqual(['a', 'b', '1']);
      expect(fromCommaSeparated('a,b,c')).toEqual(['a', 'b', 'c']);
      expect(fromCommaSeparated('   1,2,')).toEqual(['1', '2']);
    });

    it('handles newline in the ids', () => {
      expect(fromCommaSeparated('a, b, 1,  \n2')).toEqual(['a', 'b', '1', '2']);
      expect(fromCommaSeparated('\n\n\n')).toEqual([]);
      expect(fromCommaSeparated('\n\n\n1')).toEqual(['1']);
      expect(fromCommaSeparated('\n\n\n      1, 2')).toEqual(['1', '2']);
    });

    it('removes duplicates', () => {
      expect(fromCommaSeparated('a, b, 1, 1, a')).toEqual(['a', 'b', '1']);
    });
  });

  describe('submit form', () => {

    it('does not submit when there no device euids', () => {
      const state = batchReferenceReducer({...initialState, batchId: 'test'}, changeDeviceEuis(''));

      expect(state).toEqual({...initialState, batchId: 'test'});
    });

    it('does not submit when there device euids but no batch id set', () => {
      const state = batchReferenceReducer(initialState, changeDeviceEuis('a,b'));

      expect(state).toEqual({...initialState, deviceEuisText: 'a,b'});
    });

    it('submits the form if batch id when changing device euis are set', () => {
      const state = batchReferenceReducer({...initialState, batchId: 'test'}, changeDeviceEuis('a,b'));

      expect(state).toEqual({...initialState, batchId: 'test', deviceEuisText: 'a,b', canSubmitForm: true});
    });

    it('submits the form if device euis are set when changing batch id', () => {
      const payload: BatchReferencePayload = {shortPrefix: Maybe.nothing(), value: 'test'};

      const state = batchReferenceReducer({...initialState, deviceEuisText: 'a,b'}, changeBatchReference(payload));

      expect(state).toEqual({...initialState, batchId: 'test', deviceEuisText: 'a,b', canSubmitForm: true});
    });
  });

});
