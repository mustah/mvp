import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {routes} from '../../../app/routes';
import {UserActionsDropdown} from '../../../components/actions-dropdown/UserActionsDropdown';
import {DeleteUserAlert} from '../../../components/dialog/DeleteUserDialog';
import {Column} from '../../../components/layouts/column/Column';
import {Loader} from '../../../components/loading/Loader';
import {PaginationControl} from '../../../components/pagination-control/PaginationControl';
import {Table, TableColumn} from '../../../components/table/Table';
import {TableHead} from '../../../components/table/TableHead';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {deleteUser, fetchUsers} from '../../../state/domain-models/domainModelsActions';
import {getResultDomainModels} from '../../../state/domain-models/domainModelsSelectors';
import {filterUsersByUser, User} from '../../../state/domain-models/user/userModels';
import {getUserEntities, getUsersTotal} from '../../../state/domain-models/user/userSelectors';
import {changePaginationValidation} from '../../../state/ui/pagination/paginationActions';
import {OnChangePage, Pagination} from '../../../state/ui/pagination/paginationModels';
import {getPaginationList, getValidationPagination} from '../../../state/ui/pagination/paginationSelectors';
import {OnClickWithId, uuid} from '../../../types/Types';
import {UserLinkButton} from '../components/UserLinkButton';

interface StateToProps {
  currentUser: User;
  usersCount: number;
  users: DomainModel<User>;
  isFetching: boolean;
  paginatedList: uuid[];
  pagination: Pagination;
  encodedUriParametersForUsers: string;
}

interface DispatchToProps {
  deleteUser: OnClickWithId;
  fetchUsers: (encodedUriParameters: string) => void;
  paginationChangePage: OnChangePage;
}

interface State {
  isDeleteDialogOpen: boolean;
  userToDelete?: uuid;
}

class UserAdministration extends React.Component<StateToProps & DispatchToProps, State> {

  state: State = {isDeleteDialogOpen: false};

  componentDidMount() {
    const {fetchUsers, encodedUriParametersForUsers} = this.props;

    fetchUsers(encodedUriParametersForUsers);
  }

  openDialog = (id: uuid) => {
    this.setState({isDeleteDialogOpen: true, userToDelete: id});
  }

  closeDialog = () => this.setState({isDeleteDialogOpen: false});

  deleteSelectedUser = () => this.props.deleteUser(this.state.userToDelete!);

  render() {
    const {
      currentUser,
      users,
      isFetching,
      pagination,
      paginationChangePage,
      usersCount,
    } = this.props;

    const renderName = ({name}: User) => name;
    const renderEmail = ({email}: User) => email;
    const renderOrganisation = ({organisation: {name}}: User) => name;
    const renderRoles = ({roles}: User) => roles.join(', ');
    const renderActionDropdown = ({id}: User) =>
      <UserActionsDropdown openDeleteAlert={this.openDialog} id={id}/>;

    // TODO filter the companies in the backend instead, to get rid of this manipulation in the front end
    const usersToRender = filterUsersByUser(users, currentUser);
    const paginatedList = Object.keys(usersToRender);

    return (
      <Loader isFetching={isFetching}>
        <Column>
          <UserLinkButton to={routes.adminUsersAdd} text={firstUpperTranslated('add user')}/>
          <Table result={paginatedList} entities={usersToRender}>
            <TableColumn
              header={<TableHead className="first">{translate('name')}</TableHead>}
              renderCell={renderName}
            />
            <TableColumn
              header={<TableHead>{translate('email')}</TableHead>}
              renderCell={renderEmail}
            />
            <TableColumn
              header={<TableHead>{translate('organisation')}</TableHead>}
              renderCell={renderOrganisation}
            />
            <TableColumn
              header={<TableHead>{translate('roles')}</TableHead>}
              renderCell={renderRoles}
            />
            <TableColumn
              header={<TableHead className="actionDropdown">{' '}</TableHead>}
              renderCell={renderActionDropdown}
            />
          </Table>
          <PaginationControl pagination={pagination} changePage={paginationChangePage} numOfEntities={usersCount}/>
          <DeleteUserAlert
            isOpen={this.state.isDeleteDialogOpen}
            close={this.closeDialog}
            confirm={this.deleteSelectedUser}
          />
        </Column>
      </Loader>
    );
  }
}

const mapStateToProps = ({ui, domainModels: {users}, auth}: RootState): StateToProps => {
  const pagination = getValidationPagination(ui);
  return {
    usersCount: getUsersTotal(users),
    users: getUserEntities(users),
    currentUser: auth.user!,
    paginatedList: getPaginationList({pagination, result: getResultDomainModels(users)}),
    pagination,
    encodedUriParametersForUsers: '',
    isFetching: users.isFetching,
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  paginationChangePage: changePaginationValidation,
  deleteUser,
  fetchUsers,
}, dispatch);

export const UserAdministrationContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(UserAdministration);
