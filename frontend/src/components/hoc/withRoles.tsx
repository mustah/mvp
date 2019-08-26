import * as React from 'react';
import {connect} from 'react-redux';
import {compose as composeHoc} from 'recompose';
import {compose} from 'redux';
import {RootState} from '../../reducers/rootReducer';
import {User} from '../../state/domain-models/user/userModels';
import {isMvpAdmin, isMvpUserRole, isOtcUserRole, isSuperAdmin} from '../../state/domain-models/user/userSelectors';
import {Predicate} from '../../types/Types';
import {getUser} from '../../usecases/auth/authSelectors';
import {componentOrNothing} from './hocs';

interface AuthenticatedUser {
  user: User;
}

const selectUser = (authenticatedUser: AuthenticatedUser) => authenticatedUser.user;

const isMvpUser: Predicate<AuthenticatedUser> = compose(isMvpUserRole, selectUser);
const isOtcUser: Predicate<AuthenticatedUser> = compose(isOtcUserRole, selectUser);
const isMvpAdminUser: Predicate<AuthenticatedUser> = compose(isMvpAdmin, selectUser);
const isSuperAdminUser: Predicate<AuthenticatedUser> = compose(isSuperAdmin, selectUser);

const mapStateToProps = ({auth}: RootState): AuthenticatedUser => ({user: getUser(auth)});

export const withMvpUser =
  <P extends {}>(Component: React.ComponentType<P & AuthenticatedUser>) =>
    composeHoc<AuthenticatedUser, P>(connect(mapStateToProps), componentOrNothing(isMvpUser))(Component);

export const withOtcWebUser =
  <P extends {}>(Component: React.ComponentType<P & AuthenticatedUser>) =>
    composeHoc<AuthenticatedUser, P>(connect(mapStateToProps), componentOrNothing(isOtcUser))(Component);

export const withMvpAdminOnly =
  <P extends {}>(Component: React.ComponentType<P & AuthenticatedUser>) =>
    composeHoc<AuthenticatedUser, P>(connect(mapStateToProps), componentOrNothing(isMvpAdminUser))(Component);

export const withSuperAdminOnly =
  <P extends {}>(Component: React.ComponentType<P & AuthenticatedUser>) =>
    composeHoc<AuthenticatedUser, P>(connect(mapStateToProps), componentOrNothing(isSuperAdminUser))(Component);
