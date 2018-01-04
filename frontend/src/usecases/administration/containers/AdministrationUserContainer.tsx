import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {UserActionsDropdown} from '../../../components/actions-dropdown/UserActionsDropdown';
import {PaginationControl} from '../../../components/pagination-control/PaginationControl';
import {Table, TableColumn} from '../../../components/table/Table';
import {TableHead} from '../../../components/table/TableHead';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {fetchUsers} from '../../../state/domain-models/domainModelsActions';
import {getResultDomainModels} from '../../../state/domain-models/domainModelsSelectors';
import {filterUsersByUser, User} from '../../../state/domain-models/user/userModels';
import {getUserEntities, getUsersTotal} from '../../../state/domain-models/user/userSelectors';
import {changePaginationValidation} from '../../../state/ui/pagination/paginationActions';
import {OnChangePage, Pagination} from '../../../state/ui/pagination/paginationModels';
import {getPaginationList, getValidationPagination} from '../../../state/ui/pagination/paginationSelectors';
import {uuid} from '../../../types/Types';

interface StateToProps {
  currentUser: User;
  usersCount: number;
  users: DomainModel<User>;
  paginatedList: uuid[];
  pagination: Pagination;
  encodedUriParametersForUsers: string;
}

interface DispatchToProps {
  paginationChangePage: OnChangePage;
  fetchUsers: (encodedUriParameters: string) => void;
}

class UserAdministration extends React.Component<StateToProps & DispatchToProps> {
  componentDidMount() {
    const {fetchUsers, encodedUriParametersForUsers} = this.props;

    fetchUsers(encodedUriParametersForUsers);
  }

  render() {
    const {
      currentUser,
      users,
      pagination,
      paginationChangePage,
      usersCount,
    } = this.props;

    const renderName = ({name}: User) => name;
    const renderEmail = ({email}: User) => email;
    const renderCompany = ({company: {name}}: User) => name;
    const renderRoles = ({roles}: User) => roles.join(', ');
    const renderActionDropdown = ({id}: User) =>
      <UserActionsDropdown id={id} />;

    // TODO filter the companies in the backend instead, to get rid of this manipulation in the front end
    const usersToRender = filterUsersByUser(users, currentUser);
    const paginatedList = Object.keys(usersToRender);

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
            header={<TableHead>{translate('company')}</TableHead>}
            renderCell={renderCompany}
          />
          <TableColumn
            header={<TableHead>{translate('roles')}</TableHead>}
            renderCell={renderRoles}
          />
          <TableColumn
            header={<TableHead />}
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
  fetchUsers,
}, dispatch);

export const AdministrationUserContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(UserAdministration);
