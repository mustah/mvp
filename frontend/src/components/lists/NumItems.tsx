import * as React from 'react';
import {colors} from '../../app/colors';
import {dividerBorder} from '../../app/themes';
import {TotalElements} from '../../state/domain-models-paginated/paginatedDomainModels';
import {withEmptyContentComponent} from '../hoc/withEmptyContent';
import {Row} from '../layouts/row/Row';
import {Bold, Normal} from '../texts/Texts';

interface NumItemsProps extends TotalElements {
  label: string;
}

const PlaceholderWhileLoading = () => <Row style={{height: 50}}/>;

export const NumItems = withEmptyContentComponent(({label, totalElements}: NumItemsProps) => (
  <Row style={{padding: 16, marginTop: 1, borderTop: dividerBorder}}>
    <Normal style={{color: colors.info}} className="uppercase">{`${label}:`}</Normal>
    <Bold style={{marginLeft: 8}}>{totalElements}</Bold>
  </Row>
), PlaceholderWhileLoading);
