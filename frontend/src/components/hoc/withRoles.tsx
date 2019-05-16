import * as React from 'react';
import {connect} from 'react-redux';
import {compose} from 'redux';
import {RootState} from '../../reducers/rootReducer';
import {User} from '../../state/domain-models/user/userModels';
import {isAdmin, isSuperAdmin} from '../../state/domain-models/user/userSelectors';
import {getUser} from '../../usecases/auth/authSelectors';
import {componentOrNothing} from './hocs';

interface AuthenticatedUser {
  user: User;
}

const selectUser = (authenticatedUser: AuthenticatedUser) => authenticatedUser.user;
const userIsAdmin = compose(isAdmin, selectUser);
const userIsSuperAdmin = compose(isSuperAdmin, selectUser);

const onlyAdmins =
  <P extends AuthenticatedUser>(Component: React.ComponentType<P>): React.SFC<P> =>
    componentOrNothing<P>(userIsAdmin)(Component);

const mapStateToProps = ({auth}: RootState): AuthenticatedUser => ({user: getUser(auth)});

const superAdminOnly =
  <P extends AuthenticatedUser>(Component: React.ComponentType<P>): React.SFC<P> =>
    componentOrNothing<P>(userIsSuperAdmin)(Component);

export const withSuperAdminOnly =
  <OwnProps extends {}>(Component: React.ComponentType<OwnProps & AuthenticatedUser>) =>
    connect<AuthenticatedUser>(mapStateToProps)(superAdminOnly(Component));

export const withAdminOnly =
  <OwnProps extends {}>(Component: React.ComponentType<OwnProps & AuthenticatedUser>) =>
    connect<AuthenticatedUser>(mapStateToProps)(onlyAdmins(Component));
