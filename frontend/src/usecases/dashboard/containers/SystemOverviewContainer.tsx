import * as React from 'react';
import {formatDate, formatNumber} from '../../../services/formatters';
import {translate} from '../../../services/translationService';
import {Xlarge} from '../../common/components/texts/Texts';
import {PeriodSelectionContainer} from '../../common/containers/PeriodSelectionContainer';
import {Row} from '../../layouts/components/row/Row';
import {StatusBox} from '../components/status/StatusBox';

export const SystemOverviewContainer = (props) => {
    const dataPoints = 3567;
    const connectors = 3;

    return (
        <div>
            <Row>
                <Xlarge className="Bold">SYSTEM OVERVIEW</Xlarge>
            </Row>
            <Row className="Row-right">
                <PeriodSelectionContainer/>
            </Row>
            <Row>
                <StatusBox
                    title={translate('collection') + ' (%)'}
                    count={translate('{{count}} data point', {count: dataPoints})}
                    value="95.8"
                    color="orange"
                />
                <StatusBox
                    title={translate('measurement quality') + ' (%)'}
                    count={translate('{{count}} data point', {count: dataPoints})}
                    value="93.5"
                    color="red"
                />
                <StatusBox
                    title={translate('connectors') + ' (%)'}
                    count={translate('{{count}} unit', {count: connectors})}
                    value="100"
                    color="green"
                />

                <StatusBox
                    title="Tidsupplösning"
                    count={translate('a date: {{date}}', {date: formatDate(new Date())})}
                    value=""
                    color="grey"
                />
                <StatusBox
                    title=""
                    count={translate('a number: {{num}}', {num: formatNumber(123456.12341234)})}
                    value=""
                    color="grey"
                />
            </Row>
        </div>
    );
};
