import * as React from 'react';
import {connect} from 'react-redux';
import {compose} from 'redux';
import {RootState} from '../../reducers/rootReducer';
import {User} from '../../state/domain-models/user/userModels';
import {isAdmin, isSuperAdmin} from '../../state/domain-models/user/userSelectors';
import {getUser} from '../../usecases/auth/authSelectors';
import {testOrNull} from './hocs';

interface AuthenticatedUser {
  user: User;
}

const selectUser = (authenticatedUser: AuthenticatedUser) => authenticatedUser.user;
const userIsAdmin = compose(isAdmin, selectUser);
const userIsSuperAdmin = compose(isSuperAdmin, selectUser);

const onlyAdmins =
  <P extends AuthenticatedUser>(Component: React.ComponentType<P>): React.SFC<P> =>
    testOrNull<P>(userIsAdmin)(Component);

const mapStateToProps = ({auth}: RootState): AuthenticatedUser => ({user: getUser(auth)});

export const onlySuperAdmins =
  <P extends AuthenticatedUser>(Component: React.ComponentType<P>): React.SFC<P> =>
    testOrNull<P>(userIsSuperAdmin)(Component);

export const superAdminOnly =
  <OwnProps extends {}>(Component: React.ComponentType<OwnProps & AuthenticatedUser>) =>
    connect<AuthenticatedUser>(mapStateToProps)(onlySuperAdmins(Component));

export const adminOnly =
  <OwnProps extends {}>(Component: React.ComponentType<OwnProps & AuthenticatedUser>) =>
    connect<AuthenticatedUser>(mapStateToProps)(onlyAdmins(Component));
