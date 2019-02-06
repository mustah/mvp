import {emptyActionOf} from '../../types/Types';
import {loginRequest, logoutUser} from '../../usecases/auth/authActions';
import {rootReducer} from '../rootReducer';

describe('rootReducer', () => {

  const unknown = emptyActionOf('unknown');

  it('does not change the state reference when action type is unknown', () => {
    const state = rootReducer(undefined, unknown());

    expect(state).not.toEqual(undefined);
  });

  it('changes state when a known action has been dispatched', () => {
    const initialState = rootReducer(undefined, unknown());
    const state = rootReducer(initialState, loginRequest());

    expect(state).not.toBe(initialState);
  });

  it('clears state on logout', () => {
    const initialState = rootReducer(undefined, unknown());
    const state = rootReducer(initialState, loginRequest());

    expect(state).not.toEqual(initialState);
    expect(rootReducer(state, logoutUser(undefined))).toEqual(initialState);
  });

});
