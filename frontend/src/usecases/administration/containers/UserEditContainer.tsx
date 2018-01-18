import Paper from 'material-ui/Paper';
import * as React from 'react';
import {connect} from 'react-redux';
import {RouteComponentProps} from 'react-router';
import {bindActionCreators} from 'redux';
import {paperStyle} from '../../../app/themes';
import {Row} from '../../../components/layouts/row/Row';
import {WrapperIndent} from '../../../components/layouts/wrapper/Wrapper';
import {MainTitle} from '../../../components/texts/Titles';
import {PageComponent} from '../../../containers/PageComponent';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {modifyUser} from '../../../state/domain-models/domainModelsActions';
import {Organisation, Role, User} from '../../../state/domain-models/user/userModels';
import {getUserEntities} from '../../../state/domain-models/user/userSelectors';
import {OnClick, uuid} from '../../../types/Types';
import {UserEditForm} from '../../../components/forms/UserEditForm';

interface StateToProps {
  organisations: Organisation[];
  roles: Role[];
  users: DomainModel<User>;
}

interface DispatchToProps {
  modifyUser: OnClick;
}

type OwnProps = RouteComponentProps<{userId: uuid}>;

type Props = StateToProps & DispatchToProps & OwnProps;

const UserEdit = (props: Props) => {
  const {modifyUser, organisations, roles, users, match: {params: {userId}}} = props;
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
            onSubmit={modifyUser}
            possibleRoles={roles}
            isEditSelf={false}
            user={users[userId]}
          />
        </WrapperIndent>
      </Paper>
    </PageComponent>
  );
};

// TODO get organisations and roles from backend
const mapStateToProps = ({domainModels: {users}}: RootState): StateToProps => ({
  organisations: [
    {id: 1, code: 'elvaco', name: 'Elvaco'},
    {id: 2, code: 'wayne-industries', name: 'Wayne Industries'},
  ],
  roles: [
    Role.ADMIN,
    Role.USER,
  ],
  users: getUserEntities(users),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  modifyUser,
}, dispatch);

export const UserEditContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(UserEdit);
