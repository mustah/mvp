import {loginRequest} from '../../usecases/auth/authActions';
import {rootReducer, RootState} from '../rootReducer';

describe('rootReducer', () => {

  it('does not change the state reference when action type is unknown', () => {
    const initialRootState: Partial<RootState> = {};
    const initialState = rootReducer(initialRootState as RootState, {type: 'unknown'});
    const state = rootReducer(initialState, {type: 'unknown'});

    expect(state).toBe(initialState);
  });

  it('changes state when a known action has been dispatched', () => {
    const initialRootState: Partial<RootState> = {};
    const initialState = rootReducer(initialRootState as RootState, {type: 'unknown'});
    const state = rootReducer(initialState, loginRequest());

    expect(state).not.toBe(initialState);
  });
});
