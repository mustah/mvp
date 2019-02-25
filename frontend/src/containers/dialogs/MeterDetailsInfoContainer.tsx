import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {WrappedDateTime} from '../../components/dates/WrappedDateTime';
import {Column} from '../../components/layouts/column/Column';
import {Row} from '../../components/layouts/row/Row';
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
import {useFetchOrganisation} from './fetchDialogDataHook';
import {Info, SuperAdminInfo} from './Info';

const renderReadInterval = (minutes: number | undefined): string => {
  if (!minutes) {
    return translate('unknown');
  } else if (minutes >= 60) {
    return (minutes / 60) + translate('hour in short');
  } else {
    return minutes + translate('minute in short');
  }
};

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

const MeterDetailsInfo = ({
  meter: {
    address,
    readIntervalMinutes,
    collectionPercentage,
    location,
    id,
    manufacturer,
    medium,
    organisationId,
    alarms,
    statusChanged,
    facility,
    isReported,
    mbusDeviceType,
    revision,
  },
  fetchOrganisation,
  organisation,
  user
}: Props) => {
  useFetchOrganisation({fetchOrganisation, user, organisationId});

  const organisationName = organisation.map(({name}) => name).orElse(translate('unknown'));

  const formattedCollectionPercentage = formatCollectionPercentage(
    collectionPercentage,
    readIntervalMinutes,
    isSuperAdmin(user),
  );

  const sum = alarms ? alarms.reduce((previous, current) => previous + current.mask, 0) : 0;

  const alarmCode = alarms && alarms.length
    // tslint:disable-next-line:no-bitwise
    ? ('00000000' + ((sum >>> 0).toString(2))).slice(-8) // zero-padded 8-bit string
    : '-';

  return (
    <Column className="Overview">
      <Row>
        <Column>
          <Row>
            <div className="display-none">{id}</div>
            <MainTitle>{translate('meter')}</MainTitle>
          </Row>
        </Column>
        <Info label={translate('product model')}>
          <BoldFirstUpper>{orUnknown(manufacturer)}</BoldFirstUpper>
        </Info>
        <Info label={translate('medium')}>
          <BoldFirstUpper>{medium}</BoldFirstUpper>
        </Info>
        <Info label={translate('city')}>
          <CityInfo name={orUnknown(location.city)} subTitle={orUnknown(location.country)}/>
        </Info>
        <Info label={translate('address')}>
          <BoldFirstUpper>{orUnknown(location.address)}</BoldFirstUpper>
        </Info>
        <SuperAdminInfo label={translate('organisation')}>
          <BoldFirstUpper>{organisationName}</BoldFirstUpper>
        </SuperAdminInfo>
      </Row>
      <Row>
        <Column>
          <Subtitle>{translate('collection')}</Subtitle>
        </Column>
        <Info className="First-column" label={translate('resolution')}>
          <BoldFirstUpper>{renderReadInterval(readIntervalMinutes)}</BoldFirstUpper>
        </Info>
        <Info label={translate('collection percentage')}>
          <BoldFirstUpper>{formattedCollectionPercentage}</BoldFirstUpper>
        </Info>
      </Row>
      <Row>
        <Column>
          <Subtitle>{translate('validation')}</Subtitle>
        </Column>
        <Info className="First-column" label={translate('alarm')}>
          <MeterAlarm alarms={alarms}/>
        </Info>
        <Info label={translate('alarm code')}>
          <BoldFirstUpper>{alarmCode}</BoldFirstUpper>
        </Info>
        <Info label={translate('status change')}>
          <WrappedDateTime date={statusChanged} hasContent={!!statusChanged}/>
        </Info>
      </Row>
      <Row>
        <Column>
          <Subtitle>{translate('labels')}</Subtitle>
        </Column>
        <Info label={translate('facility id')}>
          <BoldFirstUpper>{facility}</BoldFirstUpper>
        </Info>
        <Info label={translate('meter id')}>
          <BoldFirstUpper>{address}</BoldFirstUpper>
        </Info>
        <Info label={translate('m-bus device type')}>
          <BoldFirstUpper>{mbusDeviceType || '-'}</BoldFirstUpper>
        </Info>
        <Info label={translate('revision')}>
          <BoldFirstUpper>{revision || '-'}</BoldFirstUpper>
        </Info>
        <ErrorLabel hasError={isReported}>{translate('reported')}</ErrorLabel>
      </Row>
    </Column>
  );
};

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
