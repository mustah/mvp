import Paper from 'material-ui/Paper';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {paperStyle} from '../../../app/themes';
import {UserEditForm} from '../../../components/forms/UserEditForm';
import {Column} from '../../../components/layouts/column/Column';
import {Row} from '../../../components/layouts/row/Row';
import {MainTitle} from '../../../components/texts/Titles';
import {MvpPageContainer} from '../../../containers/MvpPageContainer';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {Organisation} from '../../../state/domain-models/organisation/organisationModels';
import {fetchOrganisations} from '../../../state/domain-models/organisation/organisationsApiActions';
import {getOrganisations} from '../../../state/domain-models/organisation/organisationSelectors';
import {modifyProfile} from '../../../state/domain-models/user/userApiActions';
import {Role, User} from '../../../state/domain-models/user/userModels';
import {getRoles} from '../../../state/domain-models/user/userSelectors';
import {Language} from '../../../state/language/languageModels';
import {getLanguages} from '../../../state/language/languageSelectors';
import {Fetch} from '../../../types/Types';
import {getUser} from '../../auth/authSelectors';

interface StateToProps {
  user: User;
  organisations: Organisation[];
  roles: Role[];
  languages: Language[];
}

interface DispatchToProps {
  modifyProfile: (user: User) => void;
  fetchOrganisations: Fetch;
}

type Props = StateToProps & DispatchToProps;

class EditProfile extends React.Component<Props> {

  componentDidMount() {
    this.props.fetchOrganisations();
  }

  componentWillReceiveProps({fetchOrganisations}: Props) {
    fetchOrganisations();
  }

  render() {
    const {user, organisations, roles, modifyProfile, languages} = this.props;
    const userOrganisations: Organisation[] = organisations.length > 0
      ? organisations
      : [user.organisation];
    return (
      <MvpPageContainer>
        <Row className="space-between">
          <MainTitle>
            {translate('profile')}
          </MainTitle>
        </Row>
        <Paper style={paperStyle}>
          <Column className="EditProfileContainer">
            <UserEditForm
              onSubmit={modifyProfile}
              organisations={userOrganisations}
              possibleRoles={roles}
              isEditSelf={true}
              user={user}
              languages={languages}
            />
          </Column>
        </Paper>
      </MvpPageContainer>
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
  fetchOrganisations,
}, dispatch);

export const EditProfileContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(EditProfile);