import {loginRequest, logoutUser} from '../../usecases/auth/authActions';
import {rootReducer} from '../rootReducer';

describe('rootReducer', () => {

  it('does not change the state reference when action type is unknown', () => {
    const initialState = rootReducer(undefined, {type: 'unknown'});
    const state = rootReducer(initialState, {type: 'unknown'});

    expect(initialState).not.toEqual(undefined);
    expect(state).toBe(initialState);
  });

  it('changes state when a known action has been dispatched', () => {
    const initialState = rootReducer(undefined, {type: 'unknown'});
    const state = rootReducer(initialState, loginRequest());

    expect(state).not.toBe(initialState);
  });

  it('clears state on logout', () => {
    const initialState = rootReducer(undefined, {type: 'unknown'});
    const state = rootReducer(initialState, loginRequest());

    expect(state).not.toEqual(initialState);
    expect(rootReducer(state, logoutUser(undefined))).toEqual(initialState);
  });

});
