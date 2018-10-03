import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {WrappedDateTime} from '../../components/dates/WrappedDateTime';
import {Column} from '../../components/layouts/column/Column';
import {Row, RowMiddle} from '../../components/layouts/row/Row';
import {MeterAlarm} from '../../components/status/MeterAlarm';
import {ErrorLabel} from '../../components/texts/ErrorLabel';
import {CityInfo} from '../../components/texts/Labels';
import {BoldFirstUpper} from '../../components/texts/Texts';
import {MainTitle, Subtitle} from '../../components/texts/Titles';
import {formatCollectionPercentage} from '../../helpers/formatters';
import {Maybe} from '../../helpers/Maybe';
import {orUnknown} from '../../helpers/translations';
import {RootState} from '../../reducers/rootReducer';
import {translate} from '../../services/translationService';
import {getDomainModelById} from '../../state/domain-models/domainModelsSelectors';
import {MeterDetails} from '../../state/domain-models/meter-details/meterDetailsModels';
import {Organisation} from '../../state/domain-models/organisation/organisationModels';
import {fetchOrganisation} from '../../state/domain-models/organisation/organisationsApiActions';
import {User} from '../../state/domain-models/user/userModels';
import {isSuperAdmin} from '../../state/domain-models/user/userSelectors';
import {CallbackWithId} from '../../types/Types';
import {getUser} from '../../usecases/auth/authSelectors';
import {Info, SuperAdminInfo} from './Info';

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
    const {meter, organisation, user} = this.props;
    const organisationName = organisation.map((o) => o.name).orElse(translate('unknown'));

    const renderReadInterval = () => {
      if (!meter.readIntervalMinutes) {
        return translate('unknown');
      } else if (meter.readIntervalMinutes >= 60) {
        return (meter.readIntervalMinutes / 60) + translate('hour in short');
      } else {
        return meter.readIntervalMinutes + translate('minute in short');
      }
    };

    const {address, city, country} = meter.location;
    const formattedCollectionPercentage = formatCollectionPercentage(
      meter.collectionPercentage,
      meter.readIntervalMinutes,
      isSuperAdmin(user),
    );

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
            <Info label={translate('product model')}>
              <BoldFirstUpper>{orUnknown(meter.manufacturer)}</BoldFirstUpper>
            </Info>
            <Info label={translate('medium')}>
              <BoldFirstUpper>{meter.medium}</BoldFirstUpper>
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
          <Row>
            <Column>
              <Row>
                <Subtitle>{translate('collection')}</Subtitle>
              </Row>
            </Column>
            <Info className="First-column" label={translate('resolution')}>
              <BoldFirstUpper>{renderReadInterval()}</BoldFirstUpper>
            </Info>
            <Info label={translate('collection percentage')}>
              <BoldFirstUpper>{formattedCollectionPercentage}</BoldFirstUpper>
            </Info>
          </Row>
          <Row>
            <Column>
              <Row>
                <Subtitle>{translate('validation')}</Subtitle>
              </Row>
            </Column>
            <Info className="First-column" label={translate('alarm')}>
              <MeterAlarm alarm={meter.alarm}/>
            </Info>
            <Info label={translate('status change')}>
              <WrappedDateTime date={meter.statusChanged} hasContent={!!meter.statusChanged}/>
            </Info>
          </Row>
          <RowMiddle>
            <Column>
              <Row>
                <Subtitle>{translate('labels')}</Subtitle>
              </Row>
            </Column>
            <Info label={translate('facility id')}>
              <BoldFirstUpper>{meter.facility}</BoldFirstUpper>
            </Info>
            <Info label={translate('meter id')}>
              <BoldFirstUpper>{meter.address}</BoldFirstUpper>
            </Info>
            <ErrorLabel hasError={meter.isReported}>{translate('reported')}</ErrorLabel>
          </RowMiddle>
        </Column>
      </Row>
    );
  }
}

const mapStateToProps = (
  {domainModels: {organisations}, auth, paginatedDomainModels: {meters}}: RootState,
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
