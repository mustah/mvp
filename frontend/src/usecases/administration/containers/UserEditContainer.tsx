import Paper from 'material-ui/Paper';
import * as React from 'react';
import {connect} from 'react-redux';
import {RouteComponentProps} from 'react-router';
import {bindActionCreators} from 'redux';
import {paperStyle} from '../../../app/themes';
import {UserEditForm} from '../../../components/forms/UserEditForm';
import {RowIndented} from '../../../components/layouts/row/Row';
import {Loader} from '../../../components/loading/Loader';
import {PageTitle} from '../../../components/texts/Titles';
import {AdminPageComponent} from '../../../containers/PageComponent';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {ObjectsById} from '../../../state/domain-models/domainModels';
import {getEntitiesDomainModels, getError} from '../../../state/domain-models/domainModelsSelectors';
import {Organisation} from '../../../state/domain-models/organisation/organisationModels';
import {fetchOrganisations} from '../../../state/domain-models/organisation/organisationsApiActions';
import {getOrganisations} from '../../../state/domain-models/organisation/organisationSelectors';
import {clearUserError, fetchUser, modifyUser} from '../../../state/domain-models/user/userApiActions';
import {Role, User} from '../../../state/domain-models/user/userModels';
import {getRoles} from '../../../state/domain-models/user/userSelectors';
import {Language} from '../../../state/language/languageModels';
import {getLanguages} from '../../../state/language/languageSelectors';
import {CallbackWithId, ClearError, ErrorResponse, Fetch, OnClick, uuid} from '../../../types/Types';
import {getUser} from '../../auth/authSelectors';

interface StateToProps {
  organisations: Organisation[];
  roles: Role[];
  languages: Language[];
  users: ObjectsById<User>;
  isFetching: boolean;
  error: Maybe<ErrorResponse>;
}

interface DispatchToProps {
  fetchUser: CallbackWithId;
  fetchOrganisations: Fetch;
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
      modifyUser,
      organisations,
      roles,
      users,
      match: {params: {userId}},
      isFetching,
      error,
      clearError,
      languages,
    } = this.props;

    return (
      <AdminPageComponent>
        <PageTitle>
          {translate('edit user')}
        </PageTitle>

        <Paper style={paperStyle}>
          <Loader isFetching={isFetching} error={error} clearError={clearError}>
            <RowIndented>
              <UserEditForm
                organisations={organisations}
                onSubmit={modifyUser}
                possibleRoles={roles}
                isEditSelf={false}
                user={users[userId]}
                languages={languages}
              />
            </RowIndented>
          </Loader>
        </Paper>
      </AdminPageComponent>
    );
  }
}

const mapStateToProps = ({auth, domainModels: {users, organisations}}: RootState): StateToProps => ({
  users: getEntitiesDomainModels(users),
  isFetching: users.isFetching,
  error: getError(users),
  organisations: getOrganisations(organisations),
  roles: getRoles(getUser(auth)),
  languages: getLanguages(),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchUser,
  fetchOrganisations,
  modifyUser,
  clearError: clearUserError,
}, dispatch);

export const UserEditContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(UserEdit);
