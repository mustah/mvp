import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import cme2110 from '../../assets/images/cme2110.jpg';
import {WrappedDateTime} from '../../components/dates/WrappedDateTime';
import {Column} from '../../components/layouts/column/Column';
import {Row} from '../../components/layouts/row/Row';
import {Status} from '../../components/status/Status';
import {CityInfo} from '../../components/texts/Labels';
import {BoldFirstUpper} from '../../components/texts/Texts';
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
    const {
      gateway: {location: {city, country, address}, serial, productModel, status, statusChanged},
      organisation,
    } = this.props;
    const organisationName = organisation.map((o) => o.name).orElse(translate('unknown'));

    return (
      <Column className="GatewayDetailsInfo">
        <Column className="Overview">
          <Row>
            <MainTitle>{translate('gateway details')}</MainTitle>
            <Info label={translate('gateway serial')}>
              <BoldFirstUpper>{serial}</BoldFirstUpper>
            </Info>
            <Info label={translate('product model')}>
              <BoldFirstUpper>{productModel}</BoldFirstUpper>
            </Info>
            <Info label={translate('city')}>
              <CityInfo name={orUnknown(city)} subTitle={orUnknown(country)}/>
            </Info>
            <Info label={translate('address')}>
              <BoldFirstUpper>{orUnknown(address)}</BoldFirstUpper>
            </Info>
            <SuperAdminInfo label={translate('organisation')}>
              <BoldFirstUpper>{organisationName}</BoldFirstUpper>
            </SuperAdminInfo>
          </Row>
        </Column>
        <Row>
          <Column className="Gateway-image">
            <img src={cme2110} width={120}/>
          </Column>
          <Info label={translate('collection')}>
            <Status label={status.name}/>
          </Info>
          <Info className="StatusChange" label={translate('status change')}>
            <WrappedDateTime date={statusChanged} hasContent={!!statusChanged}/>
          </Info>
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
