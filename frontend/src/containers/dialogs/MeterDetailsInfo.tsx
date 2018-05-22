import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {SuperAdminComponent} from '../../components/content/SuperAdminContent';
import {Column} from '../../components/layouts/column/Column';
import {Row} from '../../components/layouts/row/Row';
import {Status} from '../../components/status/Status';
import {MainTitle, Subtitle} from '../../components/texts/Titles';
import {isSuperAdmin} from '../../helpers/hoc';
import {Maybe} from '../../helpers/Maybe';
import {orUnknown} from '../../helpers/translations';
import {RootState} from '../../reducers/rootReducer';
import {translate} from '../../services/translationService';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {Organisation} from '../../state/domain-models/organisation/organisationModels';
import {fetchOrganisation} from '../../state/domain-models/organisation/organisationsApiActions';
import {getOrganisation} from '../../state/domain-models/organisation/organisationSelectors';
import {User} from '../../state/domain-models/user/userModels';
import {CallbackWithId} from '../../types/Types';
import {getUser} from '../../usecases/auth/authSelectors';
import {Info} from './Info';

interface OwnProps {
  meter: Meter;
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
    const organisationName = organisation.map((o) => o.name).orElse('unknown');

    const renderReadInterval = () => {
      if (meter.readIntervalMinutes === 0 || meter.readIntervalMinutes === undefined) {
        return translate('unknown');
      } else if (meter.readIntervalMinutes >= 60) {
        return (meter.readIntervalMinutes / 60) + translate('hour in short');
      }

      return meter.readIntervalMinutes + translate('minute in short');
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
            <SuperAdminComponent>
              <Info label={translate('organisation')} value={organisationName}/>
            </SuperAdminComponent>
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
  organisation: getOrganisation(organisations, meter.organisationId),
  user: getUser(auth),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchOrganisation,
}, dispatch);

export const MeterDetailsInfoContainer = connect<StateToProps, DispatchToProps, OwnProps>(
  mapStateToProps,
  mapDispatchToProps,
)(MeterDetailsInfo);
