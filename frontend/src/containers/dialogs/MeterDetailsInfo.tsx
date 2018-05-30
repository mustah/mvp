import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Column} from '../../components/layouts/column/Column';
import {Row} from '../../components/layouts/row/Row';
import {Status} from '../../components/status/Status';
import {MainTitle, Subtitle} from '../../components/texts/Titles';
import {isSuperAdmin} from '../../helpers/hoc';
import {Maybe} from '../../helpers/Maybe';
import {orUnknown} from '../../helpers/translations';
import {RootState} from '../../reducers/rootReducer';
import {translate} from '../../services/translationService';
import {getDomainModelById} from '../../state/domain-models/domainModelsSelectors';
import {Organisation} from '../../state/domain-models/organisation/organisationModels';
import {fetchOrganisation} from '../../state/domain-models/organisation/organisationsApiActions';
import {User} from '../../state/domain-models/user/userModels';
import {CallbackWithId} from '../../types/Types';
import {getUser} from '../../usecases/auth/authSelectors';
import {Info, SuperAdminInfo} from './Info';
import {MeterDetails} from '../../state/domain-models/meter-details/meterDetailsModels';

interface OwnProps {
  meter: MeterDetails;
}

interface DispatchToProps {
  fetchOrganisation: CallbackWithId;
}

interface StateToProps {
  organisation: Maybe<Organisation>;
  user: User;
}

type Props = OwnProps & StateToProps & DispatchToProps;

class MeterDetailsInfo extends React.Component<Props> {

  componentDidMount() {
    const {fetchOrganisation, meter, user} = this.props;
    if (isSuperAdmin(user)) {
      fetchOrganisation(meter.organisationId);
    }
  }

  componentWillReceiveProps({fetchOrganisation, meter, user}: Props) {
    if (isSuperAdmin(user)) {
      fetchOrganisation(meter.organisationId);
    }
  }

  render() {
    const {meter, organisation} = this.props;
    const organisationName = organisation.map((o) => o.name).orElse(translate('unknown'));

    const renderReadInterval = () => {
      if (meter.readIntervalMinutes === 0 || meter.readIntervalMinutes === undefined) {
        return translate('unknown');
      } else if (meter.readIntervalMinutes >= 60) {
        return (meter.readIntervalMinutes / 60) + translate('hour in short');
      } else {
        return meter.readIntervalMinutes + translate('minute in short');
      }
    };

    const {city, address} = meter.location;

    return (
      <Row>
        <Column className="Overview">
          <Row>
            <Column>
              <Row>
                <div className="display-none">{meter.id}</div>
                <MainTitle>{translate('meter')}</MainTitle>
              </Row>
            </Column>
            <Info label={translate('product model')} value={meter.manufacturer}/>
            <Info label={translate('medium')} value={meter.medium}/>
            <Info label={translate('city')} value={orUnknown(city.name)}/>
            <Info label={translate('address')} value={orUnknown(address.name)}/>
            <SuperAdminInfo label={translate('organisation')} value={organisationName}/>
          </Row>
          <Row>
            <Column>
              <Row>
                <Subtitle>{translate('collection')}</Subtitle>
              </Row>
            </Column>
            <Info label={translate('resolution')} value={renderReadInterval()}/>
          </Row>
          <Row>
            <Column>
              <Row>
                <Subtitle>{translate('validation')}</Subtitle>
              </Row>
            </Column>
            <Info
              label={translate('status')}
              value={<Status name={meter.status.name}/>}
            />
          </Row>
          <Row>
            <Column>
              <Row>
                <Subtitle>{translate('labels')}</Subtitle>
              </Row>
            </Column>
            <Info label={translate('facility id')} value={meter.facility}/>
          </Row>
        </Column>
      </Row>
    );
  }
}

const mapStateToProps = (
  {domainModels: {organisations}, auth}: RootState,
  {meter}: OwnProps,
): StateToProps => ({
  organisation: getDomainModelById<Organisation>(meter.organisationId)(organisations),
  user: getUser(auth),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchOrganisation,
}, dispatch);

export const MeterDetailsInfoContainer = connect<StateToProps, DispatchToProps, OwnProps>(
  mapStateToProps,
  mapDispatchToProps,
)(MeterDetailsInfo);
