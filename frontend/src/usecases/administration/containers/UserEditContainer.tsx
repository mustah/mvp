import Paper from 'material-ui/Paper';
import * as React from 'react';
import {connect} from 'react-redux';
import {RouteComponentProps} from 'react-router';
import {bindActionCreators} from 'redux';
import {paperStyle} from '../../../app/themes';
import {OnChangePassword, PasswordEditForm} from '../../../components/forms/PasswordEditForm';
import {UserEditForm} from '../../../components/forms/UserEditForm';
import {Column} from '../../../components/layouts/column/Column';
import {AdminPageLayout} from '../../../components/layouts/layout/PageLayout';
import {Row} from '../../../components/layouts/row/Row';
import {RetryLoader} from '../../../components/loading/Loader';
import {MainTitle} from '../../../components/texts/Titles';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {ObjectsById} from '../../../state/domain-models/domainModels';
import {getAllEntities, getEntitiesDomainModels, getError} from '../../../state/domain-models/domainModelsSelectors';
import {Organisation} from '../../../state/domain-models/organisation/organisationModels';
import {fetchOrganisations} from '../../../state/domain-models/organisation/organisationsApiActions';
import {changePassword, clearUserError, fetchUser, modifyUser} from '../../../state/domain-models/user/userApiActions';
import {Role, User} from '../../../state/domain-models/user/userModels';
import {getRoles} from '../../../state/domain-models/user/userSelectors';
import {Language} from '../../../state/language/languageModels';
import {getLanguages} from '../../../state/language/languageSelectors';
import {CallbackWithId, ClearError, ErrorResponse, Fetch, OnClickEventHandler} from '../../../types/Types';
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
  modifyUser: OnClickEventHandler;
  changePassword: OnChangePassword;
  clearError: ClearError;
}

type OwnProps = RouteComponentProps<{userId: string}>;

type Props = StateToProps & DispatchToProps & OwnProps;

const userEditStyle: React.CSSProperties = {
  marginRight: 24,
};

const passwordChangeStyle: React.CSSProperties = {
  marginLeft: 24,
};

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
      changePassword,
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
      <AdminPageLayout>
        <MainTitle>{translate('edit user')}</MainTitle>

        <Paper style={{...paperStyle, padding: 24}}>
          <RetryLoader isFetching={isFetching} error={error} clearError={clearError}>
            <Row>
              <Column style={userEditStyle}>
                <UserEditForm
                  organisations={organisations}
                  onSubmit={modifyUser}
                  possibleRoles={roles}
                  isEditSelf={false}
                  user={users[userId]}
                  languages={languages}
                />
              </Column>

              <Column style={passwordChangeStyle}>
                <PasswordEditForm onSubmit={changePassword} user={users[userId]}/>
              </Column>
            </Row>
          </RetryLoader>
        </Paper>
      </AdminPageLayout>
    );
  }
}

const mapStateToProps = ({auth, domainModels: {users, organisations}}: RootState): StateToProps => ({
  users: getEntitiesDomainModels(users),
  isFetching: users.isFetching,
  error: getError(users),
  organisations: getAllEntities(organisations),
  roles: getRoles(getUser(auth)),
  languages: getLanguages(),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchUser,
  fetchOrganisations,
  modifyUser,
  changePassword,
  clearError: clearUserError,
}, dispatch);

export const UserEditContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(UserEdit);
