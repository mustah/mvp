import Paper from 'material-ui/Paper';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {paperStyle} from '../../../app/themes';
import {PasswordEditForm} from '../../../components/forms/PasswordEditForm';
import {UserEditForm} from '../../../components/forms/UserEditForm';
import {Column} from '../../../components/layouts/column/Column';
import {Row} from '../../../components/layouts/row/Row';
import {MainTitle} from '../../../components/texts/Titles';
import {PageLayout} from '../../../components/layouts/layout/PageLayout';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {Organisation} from '../../../state/domain-models/organisation/organisationModels';
import {fetchOrganisations} from '../../../state/domain-models/organisation/organisationsApiActions';
import {getOrganisations} from '../../../state/domain-models/organisation/organisationSelectors';
import {changePassword, modifyProfile} from '../../../state/domain-models/user/userApiActions';
import {Password, Role, User} from '../../../state/domain-models/user/userModels';
import {getRoles} from '../../../state/domain-models/user/userSelectors';
import {Language} from '../../../state/language/languageModels';
import {getLanguages} from '../../../state/language/languageSelectors';
import {Fetch, uuid} from '../../../types/Types';
import {getUser} from '../../auth/authSelectors';

interface StateToProps {
  user: User;
  organisations: Organisation[];
  roles: Role[];
  languages: Language[];
}

interface DispatchToProps {
  modifyProfile: (user: User) => void;
  changePassword: (password: Password, userId: uuid) => void;
  fetchOrganisations: Fetch;
}

type Props = StateToProps & DispatchToProps;

const userEditStyle: React.CSSProperties = {
  marginRight: 24,
};

const passwordChangeStyle: React.CSSProperties = {
  marginLeft: 24,
};

class EditProfile extends React.Component<Props> {

  componentDidMount() {
    this.props.fetchOrganisations();
  }

  componentWillReceiveProps({fetchOrganisations}: Props) {
    fetchOrganisations();
  }

  render() {
    const {user, organisations, roles, modifyProfile, changePassword, languages} = this.props;
    const userOrganisations: Organisation[] = organisations.length > 0
      ? organisations
      : [user.organisation];
    return (
      <PageLayout>
        <Row className="space-between">
          <MainTitle>
            {translate('profile')}
          </MainTitle>
        </Row>
        <Paper style={paperStyle}>
          <Row>
            <Column style={userEditStyle}>
              <UserEditForm
                onSubmit={modifyProfile}
                organisations={userOrganisations}
                possibleRoles={roles}
                isEditSelf={true}
                user={user}
                languages={languages}
              />
            </Column>
            <Column style={passwordChangeStyle}>
              <PasswordEditForm
                onSubmit={changePassword}
                user={user}
              />
            </Column>
          </Row>
        </Paper>
      </PageLayout>
    );
  }
}

const mapStateToProps = ({auth, domainModels: {organisations}}: RootState): StateToProps => ({
  user: getUser(auth),
  organisations: getOrganisations(organisations),
  roles: getRoles(getUser(auth)),
  languages: getLanguages(),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  modifyProfile,
  changePassword,
  fetchOrganisations,
}, dispatch);

export const EditProfileContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(EditProfile);
