import * as React from 'react';
import {connect} from 'react-redux';
import {compose as composeHoc} from 'recompose';
import {compose} from 'redux';
import {RootState} from '../../reducers/rootReducer';
import {User} from '../../state/domain-models/user/userModels';
import {isAdmin, isSuperAdmin} from '../../state/domain-models/user/userSelectors';
import {Predicate} from '../../types/Types';
import {getUser} from '../../usecases/auth/authSelectors';
import {componentOrNothing} from './hocs';

interface AuthenticatedUser {
  user: User;
}

const selectUser = (authenticatedUser: AuthenticatedUser) => authenticatedUser.user;
const issAdminUser: Predicate<AuthenticatedUser> = compose(isAdmin, selectUser);
const isSuperAdminUser: Predicate<AuthenticatedUser> = compose(isSuperAdmin, selectUser);

const withAdminOrNothing =
  <P extends AuthenticatedUser>(Component: React.ComponentType<P>): React.FunctionComponent<P> =>
    componentOrNothing<P>(issAdminUser)(Component);

const withSuperAdminOrNothing =
  <P extends AuthenticatedUser>(Component: React.ComponentType<P>): React.FunctionComponent<P> =>
    componentOrNothing<P>(isSuperAdminUser)(Component);

const mapStateToProps = ({auth}: RootState): AuthenticatedUser => ({user: getUser(auth)});

export const withAdminOnly =
  <P extends {}>(Component: React.ComponentType<P & AuthenticatedUser>) =>
    composeHoc<AuthenticatedUser, P>(connect(mapStateToProps), withAdminOrNothing)(Component);

export const withSuperAdminOnly =
  <P extends {}>(Component: React.ComponentType<P & AuthenticatedUser>) =>
    composeHoc<AuthenticatedUser, P>(connect(mapStateToProps), withSuperAdminOrNothing)(Component);
