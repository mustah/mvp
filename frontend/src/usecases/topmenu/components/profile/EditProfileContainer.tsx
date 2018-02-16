import Paper from 'material-ui/Paper';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {paperStyle} from '../../../../app/themes';
import {Column} from '../../../../components/layouts/column/Column';
import {Row} from '../../../../components/layouts/row/Row';
import {MainTitle} from '../../../../components/texts/Titles';
import {MvpPageContainer} from '../../../../containers/MvpPageContainer';
import {RootState} from '../../../../reducers/rootReducer';
import {translate} from '../../../../services/translationService';
import {RestGet} from '../../../../state/domain-models/domainModels';
import {fetchOrganisations, modifyProfile} from '../../../../state/domain-models/domainModelsActions';
import {Organisation, Role, User} from '../../../../state/domain-models/user/userModels';
import {UserEditForm} from '../../../../components/forms/UserEditForm';
import {getOrganisations} from '../../../../state/domain-models/user/userSelectors';

interface StateToProps {
  user: User;
  organisations: Organisation[];
  roles: Role[];
}

interface DispatchToProps {
  modifyProfile: (user: User) => void;
  fetchOrganisations: RestGet;
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
    const {user, organisations, roles, modifyProfile} = this.props;
    // TODO: Add validation for fields.
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
              organisations={organisations}
              possibleRoles={roles}
              isEditSelf={true}
              user={user}
            />
          </Column>
        </Paper>
      </MvpPageContainer>
    );
  }
}

const mapStateToProps = ({auth: {user}, domainModels: {organisations}}: RootState): StateToProps => ({
  user: user!,
  organisations: getOrganisations(organisations),
  roles: [
    Role.ADMIN,
    Role.USER,
  ],
}); // TODO: Perhaps use a selector instead of using the "!" null protection for user.

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  modifyProfile,
  fetchOrganisations,
}, dispatch);

export const EditProfileContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(EditProfile);
