import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {getDomainModel, getError} from '../../../state/domain-models/domainModelsSelectors';
import {clearUserError, deleteUser, fetchUsers} from '../../../state/domain-models/user/userApiActions';
import {User} from '../../../state/domain-models/user/userModels';
import {ClearError, ErrorResponse, Fetch, OnClickWithId} from '../../../types/Types';
import {UserList} from '../components/UserList';

export interface StateToProps {
  users: DomainModel<User>;
  isFetching: boolean;
  error: Maybe<ErrorResponse>;
}

export interface DispatchToProps {
  deleteUser: OnClickWithId;
  fetchUsers: Fetch;
  clearError: ClearError;
}

const mapStateToProps = ({domainModels: {users}}: RootState): StateToProps => ({
  users: getDomainModel(users),
  isFetching: users.isFetching,
  error: getError(users),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  deleteUser,
  fetchUsers,
  clearError: clearUserError,
}, dispatch);

export const UsersContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(UserList);
