import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {imagePathFor} from '../../app/routes';
import {Column} from '../../components/layouts/column/Column';
import {Row} from '../../components/layouts/row/Row';
import {Status} from '../../components/status/Status';
import {MainTitle} from '../../components/texts/Titles';
import {Maybe} from '../../helpers/Maybe';
import {orUnknown} from '../../helpers/translations';
import {RootState} from '../../reducers/rootReducer';
import {translate} from '../../services/translationService';
import {Gateway} from '../../state/domain-models-paginated/gateway/gatewayModels';
import {getDomainModelById} from '../../state/domain-models/domainModelsSelectors';
import {Organisation} from '../../state/domain-models/organisation/organisationModels';
import {fetchOrganisation} from '../../state/domain-models/organisation/organisationsApiActions';
import {User} from '../../state/domain-models/user/userModels';
import {isSuperAdmin} from '../../state/domain-models/user/userSelectors';
import {CallbackWithId} from '../../types/Types';
import {getUser} from '../../usecases/auth/authSelectors';
import {Info, SuperAdminInfo} from './Info';

interface OwnProps {
  gateway: Gateway;
}

interface DispatchToProps {
  fetchOrganisation: CallbackWithId;
}

interface StateToProps {
  organisation: Maybe<Organisation>;
  user: User;
}

type Props = OwnProps & StateToProps & DispatchToProps;

class GatewayDetailsInfo extends React.Component<Props> {

  componentDidMount() {
    const {fetchOrganisation, gateway, user} = this.props;
    if (isSuperAdmin(user)) {
      fetchOrganisation(gateway.organisationId);
    }
  }

  componentWillReceiveProps({fetchOrganisation, gateway, user}: Props) {
    if (isSuperAdmin(user)) {
      fetchOrganisation(gateway.organisationId);
    }
  }

  render() {
    const {gateway: {location: {city, address}, serial, productModel, status}, organisation} = this.props;
    const gatewayImage = imagePathFor('cme2110.jpg');
    const organisationName = organisation.map((o) => o.name).orElse(translate('unknown'));

    return (
      <Column className="GatewayDetailsInfo">
        <Column className="Overview">
          <Row>
            <MainTitle>{translate('gateway details')}</MainTitle>
            <Info label={translate('gateway serial')} value={serial}/>
            <Info label={translate('product model')} value={productModel}/>
            <Info label={translate('city')} value={orUnknown(city.name)}/>
            <Info label={translate('address')} value={orUnknown(address.name)}/>
            <SuperAdminInfo label={translate('organisation')} value={organisationName}/>
          </Row>
        </Column>
        <Row>
          <Column>
            <img src={gatewayImage} width={120}/>
          </Column>
          <Info
            label={translate('collection')}
            value={<Status name={status.name}/>}
          />
        </Row>
      </Column>
    );
  }
}

const mapStateToProps = (
  {domainModels: {organisations}, auth}: RootState,
  {gateway}: OwnProps,
): StateToProps => ({
  organisation: getDomainModelById<Organisation>(gateway.organisationId)(organisations),
  user: getUser(auth),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchOrganisation,
}, dispatch);

export const GatewayDetailsInfoContainer = connect<StateToProps, DispatchToProps, OwnProps>(
  mapStateToProps,
  mapDispatchToProps,
)(GatewayDetailsInfo);
