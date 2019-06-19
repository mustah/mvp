import * as React from 'react';
import {connect} from 'react-redux';
import {compose as composeHoc} from 'recompose';
import {compose} from 'redux';
import {RootState} from '../../reducers/rootReducer';
import {User} from '../../state/domain-models/user/userModels';
import {isMvpAdmin, isSuperAdmin} from '../../state/domain-models/user/userSelectors';
import {Predicate} from '../../types/Types';
import {getUser} from '../../usecases/auth/authSelectors';
import {componentOrNothing} from './hocs';

interface AuthenticatedUser {
  user: User;
}

const selectUser = (authenticatedUser: AuthenticatedUser) => authenticatedUser.user;

const isMvpAdminUser: Predicate<AuthenticatedUser> = compose(isMvpAdmin, selectUser);
const isSuperAdminUser: Predicate<AuthenticatedUser> = compose(isSuperAdmin, selectUser);

const withMvpAdminOrNothing =
  <P extends AuthenticatedUser>(Component: React.ComponentType<P>): React.FunctionComponent<P> =>
    componentOrNothing<P>(isMvpAdminUser)(Component);

const withSuperAdminOrNothing =
  <P extends AuthenticatedUser>(Component: React.ComponentType<P>): React.FunctionComponent<P> =>
    componentOrNothing<P>(isSuperAdminUser)(Component);

const mapStateToProps = ({auth}: RootState): AuthenticatedUser => ({user: getUser(auth)});

export const withMvpAdminOnly =
  <P extends {}>(Component: React.ComponentType<P & AuthenticatedUser>) =>
    composeHoc<AuthenticatedUser, P>(connect(mapStateToProps), withMvpAdminOrNothing)(Component);

export const withSuperAdminOnly =
  <P extends {}>(Component: React.ComponentType<P & AuthenticatedUser>) =>
    composeHoc<AuthenticatedUser, P>(connect(mapStateToProps), withSuperAdminOrNothing)(Component);
