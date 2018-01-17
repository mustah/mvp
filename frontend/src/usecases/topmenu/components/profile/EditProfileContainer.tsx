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
import {modifyProfile} from '../../../../state/domain-models/domainModelsActions';
import {Organisation, Role, User} from '../../../../state/domain-models/user/userModels';
import {UserEditForm} from '../../../forms/components/UserEditForm';

interface StateToProps {
  user: User;
  organisations: Organisation[];
  roles: Role[];
}

interface DispatchToProps {
  modifyProfile: (user: User) => void;
}

type Props = StateToProps & DispatchToProps;

const EditProfile = ({user, organisations, roles, modifyProfile}: Props) => {

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
};

const mapStateToProps = ({auth: {user}}: RootState): StateToProps => ({
  user: user!,
  organisations: [
    {id: 1, code: 'elvaco', name: 'Elvaco'},
    {id: 2, code: 'wayne-industries', name: 'Wayne Industries'},
  ],
  roles: [
    Role.ADMIN,
    Role.USER,
  ],
}); // TODO: Perhaps use a selector instead of using the "!" null protection for user.

const mapDispatchToProps = (dispatch) => bindActionCreators({
  modifyProfile,
}, dispatch);

export const EditProfileContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(EditProfile);
