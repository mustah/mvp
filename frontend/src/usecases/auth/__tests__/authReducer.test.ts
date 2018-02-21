import {makeToken} from '../../../services/authService';
import {Role, User} from '../../../state/domain-models/user/userModels';
import {authSetUser, loginFailure, loginRequest, loginSuccess, logoutUser} from '../authActions';
import {Authorized, AuthState, Unauthorized} from '../authModels';
import {auth, initialAuthState} from '../authReducer';

describe('authReducer', () => {

  const requestState: AuthState = {isAuthenticated: false, isLoading: true};
  const password = '1234';
  const user: User = {
    id: 1,
    name: 'clark',
    email: 'ck@dailyplanet.net',
    organisation: {id: 'daily planet', name: 'daily planet', code: 'daily-planet'},
    roles: [Role.USER],
  };
  const token = makeToken(user.email, password);
  const loggedInState: AuthState = {isLoading: false, isAuthenticated: true, token, user};

  it('sends a login request', () => {
    expect(auth(initialAuthState, loginRequest())).toEqual({
      ...initialAuthState,
      isLoading: true,
      isAuthenticated: false,
    });
  });

  it('successfully logs in a user', () => {
    const payload: Authorized = {user, token};

    expect(auth(requestState, loginSuccess(payload))).toEqual({
      ...requestState,
      isLoading: false,
      isAuthenticated: true,
      token,
      user,
    });
  });

  it('fails to login a user', () => {
    const unauthorized = 401;
    const errorMessage: Unauthorized = {
      status: unauthorized,
      timestamp: Date.now(),
      error: 'Unauthorized',
      message: 'User is not authorized',
      path: '/v1/api/authenticate',
    };

    expect(auth(requestState, loginFailure(errorMessage))).toEqual({
      ...requestState,
      isLoading: false,
      isAuthenticated: false,
      error: errorMessage,
    });
  });

  it('successfully logs out a user', () => {
    expect(auth({...loggedInState, isLoading: true}, logoutUser())).toEqual({
      ...initialAuthState,
    });
  });

  it('updates the logged in users information', () => {
    const newName = 'eva nilsson';
    const modifiedUser = {...user, name: newName};

    expect(auth(loggedInState, authSetUser(modifiedUser))).toEqual({
      ...loggedInState,
      user: modifiedUser,
    });
  });
});
