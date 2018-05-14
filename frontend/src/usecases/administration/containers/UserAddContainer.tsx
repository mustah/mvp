import Paper from 'material-ui/Paper';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {paperStyle} from '../../../app/themes';
import {UserEditForm} from '../../../components/forms/UserEditForm';
import {Row} from '../../../components/layouts/row/Row';
import {WrapperIndent} from '../../../components/layouts/wrapper/Wrapper';
import {MainTitle} from '../../../components/texts/Titles';
import {PageComponent} from '../../../containers/PageComponent';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {Organisation} from '../../../state/domain-models/organisation/organisationModels';
import {fetchOrganisations} from '../../../state/domain-models/organisation/organisationsApiActions';
import {getOrganisations} from '../../../state/domain-models/organisation/organisationSelectors';
import {addUser} from '../../../state/domain-models/user/userApiActions';
import {Role} from '../../../state/domain-models/user/userModels';
import {getRoles} from '../../../state/domain-models/user/userSelectors';
import {Language} from '../../../state/language/languageModels';
import {getLanguages} from '../../../state/language/languageSelectors';
import {OnClick, Fetch} from '../../../types/Types';

interface StateToProps {
  organisations: Organisation[];
  roles: Role[];
  languages: Language[];
}

interface DispatchToProps {
  addUser: OnClick;
  fetchOrganisations: Fetch;
}

type Props = DispatchToProps & StateToProps;

class UserAdd extends React.Component<Props> {

  componentDidMount() {
    this.props.fetchOrganisations();
  }

  componentWillReceiveProps({fetchOrganisations}: Props) {
    fetchOrganisations();
  }

  render() {
    const {addUser, organisations, roles, languages} = this.props;
    return (
      <PageComponent isSideMenuOpen={false}>
        <Row className="space-between">
          <MainTitle>
            {translate('add user')}
          </MainTitle>
        </Row>

        <Paper style={paperStyle}>
          <WrapperIndent>
            <UserEditForm
              organisations={organisations}
              onSubmit={addUser}
              possibleRoles={roles}
              isEditSelf={false}
              languages={languages}
            />
          </WrapperIndent>
        </Paper>
      </PageComponent>
    );
  }
}

const mapStateToProps = ({domainModels: {organisations}, auth: {user}}: RootState): StateToProps => ({
  organisations: getOrganisations(organisations),
  roles: getRoles(user!),
  languages: getLanguages(),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  addUser,
  fetchOrganisations,
}, dispatch);

export const UserAddContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(UserAdd);
