import Paper from 'material-ui/Paper';
import * as React from 'react';
import {connect} from 'react-redux';
import {RouteComponentProps} from 'react-router';
import {bindActionCreators} from 'redux';
import {paperStyle} from '../../../app/themes';
import {UserEditForm} from '../../../components/forms/UserEditForm';
import {Row} from '../../../components/layouts/row/Row';
import {WrapperIndent} from '../../../components/layouts/wrapper/Wrapper';
import {Loader} from '../../../components/loading/Loader';
import {MainTitle} from '../../../components/texts/Titles';
import {PageComponent} from '../../../containers/PageComponent';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {ClearError, ObjectsById, RestGet} from '../../../state/domain-models/domainModels';
import {
  clearErrorUsers,
  fetchUser,
  modifyUser,
} from '../../../state/domain-models/domainModelsActions';
import {
  getEntitiesDomainModels,
  getError,
} from '../../../state/domain-models/domainModelsSelectors';
import {fetchOrganisations} from '../../../state/domain-models/organisation/organisationsApiActions';
import {Organisation, Role, User} from '../../../state/domain-models/user/userModels';
import {getOrganisations, getRoles} from '../../../state/domain-models/user/userSelectors';
import {ErrorResponse, OnClick, uuid} from '../../../types/Types';

interface StateToProps {
  organisations: Organisation[];
  roles: Role[];
  users: ObjectsById<User>;
  isFetching: boolean;
  error: Maybe<ErrorResponse>;
}

interface DispatchToProps {
  fetchUser: (id: uuid) => void;
  fetchOrganisations: RestGet;
  modifyUser: OnClick;
  clearError: ClearError;
}

type OwnProps = RouteComponentProps<{userId: uuid}>;

type Props = StateToProps & DispatchToProps & OwnProps;

class UserEdit extends React.Component<Props, {}> {

  componentDidMount() {
    const {match: {params: {userId}}, fetchUser, fetchOrganisations} = this.props;
    fetchOrganisations();
    fetchUser(userId);
  }

  componentWillReceiveProps({match: {params: {userId}}, fetchUser, fetchOrganisations}: Props) {
    fetchOrganisations();
    fetchUser(userId);
  }

  render() {
    const {
      modifyUser, organisations, roles, users, match: {params: {userId}}, isFetching, error, clearError,
    } = this.props;

    return (
      <PageComponent isSideMenuOpen={false}>
        <Row className="space-between">
          <MainTitle>
            {translate('edit user')}
          </MainTitle>
        </Row>

        <Paper style={paperStyle}>
          <Loader isFetching={isFetching} error={error} clearError={clearError}>
            <WrapperIndent>
              <UserEditForm
                organisations={organisations}
                onSubmit={modifyUser}
                possibleRoles={roles}
                isEditSelf={false}
                user={users[userId]}
              />
            </WrapperIndent>
          </Loader>
        </Paper>
      </PageComponent>
    );
  }
}

// TODO get organisations and roles from backend
const mapStateToProps = ({auth: {user}, domainModels: {users, organisations}}: RootState): StateToProps => ({
  users: getEntitiesDomainModels(users),
  isFetching: users.isFetching,
  error: getError(users),
  organisations: getOrganisations(organisations),
  roles: getRoles(user!),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchUser,
  fetchOrganisations,
  modifyUser,
  clearError: clearErrorUsers,
}, dispatch);

export const UserEditContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(UserEdit);
