import Divider from 'material-ui/Divider';
import * as React from 'react';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom';
import {bindActionCreators} from 'redux';
import {routes} from '../../../app/routes';
import {ActionMenuItem} from '../../../components/actions-dropdown/ActionMenuItem';
import {ActionsDropdown, MenuItems} from '../../../components/actions-dropdown/ActionsDropdown';
import {UserActionsDropdown} from '../../../components/actions-dropdown/UserActionsDropdown';
import {PaginationControl} from '../../../components/pagination-control/PaginationControl';
import {Table, TableColumn} from '../../../components/table/Table';
import {TableHead} from '../../../components/table/TableHead';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {deleteUser, fetchUsers} from '../../../state/domain-models/domainModelsActions';
import {getResultDomainModels} from '../../../state/domain-models/domainModelsSelectors';
import {filterUsersByUser, User} from '../../../state/domain-models/user/userModels';
import {getUserEntities, getUsersTotal} from '../../../state/domain-models/user/userSelectors';
import {changePaginationValidation} from '../../../state/ui/pagination/paginationActions';
import {OnChangePage, Pagination} from '../../../state/ui/pagination/paginationModels';
import {getPaginationList, getValidationPagination} from '../../../state/ui/pagination/paginationSelectors';
import {OnClickWithId, uuid} from '../../../types/Types';

interface StateToProps {
  currentUser: User;
  usersCount: number;
  users: DomainModel<User>;
  paginatedList: uuid[];
  pagination: Pagination;
  encodedUriParametersForUsers: string;
}

interface DispatchToProps {
  deleteUser: OnClickWithId;
  fetchUsers: (encodedUriParameters: string) => void;
  paginationChangePage: OnChangePage;
}

class UserAdministration extends React.Component<StateToProps & DispatchToProps> {
  componentDidMount() {
    const {fetchUsers, encodedUriParametersForUsers} = this.props;

    fetchUsers(encodedUriParametersForUsers);
  }

  render() {
    const {
      currentUser,
      deleteUser,
      users,
      pagination,
      paginationChangePage,
      usersCount,
    } = this.props;

    const renderName = ({name}: User) => name;
    const renderEmail = ({email}: User) => email;
    const renderOrganisation = ({organisation: {name}}: User) => name;
    const renderRoles = ({roles}: User) => roles.join(', ');
    const renderActionDropdown = ({id}: User) =>
      <UserActionsDropdown deleteUser={deleteUser} id={id}/>;

    // TODO filter the companies in the backend instead, to get rid of this manipulation in the front end
    const usersToRender = filterUsersByUser(users, currentUser);
    const paginatedList = Object.keys(usersToRender);

    const menuItems: MenuItems = [(
      <Link to={routes.adminUsersAdd} className="link" key="add user">
        <ActionMenuItem name={translate('add user')} />
      </Link>),
      <Divider key="a divider"/>,
      <ActionMenuItem name={translate('export to Excel (.csv)')} key="export to excel"/>,
      <ActionMenuItem name={translate('export to JSON')} key="export to json"/>,
    ];

    return (
      <div>
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
            header={<TableHead><ActionsDropdown menuItems={menuItems}/></TableHead>}
            renderCell={renderActionDropdown}
          />
        </Table>
        <PaginationControl pagination={pagination} changePage={paginationChangePage} numOfEntities={usersCount}/>
      </div>
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
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  paginationChangePage: changePaginationValidation,
  deleteUser,
  fetchUsers,
}, dispatch);

export const UserAdministrationContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(UserAdministration);
