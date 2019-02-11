import {Grid, GridCellProps, GridColumn} from '@progress/kendo-react-grid';
import {toArray} from 'lodash';
import * as React from 'react';
import {borderRadius, gridStyle} from '../../../app/themes';
import {ButtonDelete} from '../../../components/buttons/ButtonDelete';
import {ButtonLinkBlue} from '../../../components/buttons/ButtonLink';
import {ButtonVisibility} from '../../../components/buttons/ButtonVisibility';
import {IconIndicator} from '../../../components/icons/IconIndicator';
import {Column} from '../../../components/layouts/column/Column';
import {Row, RowMiddle} from '../../../components/layouts/row/Row';
import {isDefined} from '../../../helpers/commonUtils';
import {orUnknown} from '../../../helpers/translations';
import {translate} from '../../../services/translationService';
import {Normalized} from '../../../state/domain-models/domainModels';
import {Medium} from '../../../state/ui/graph/measurement/measurementModels';
import {OnClick, OnClickWithId, uuid} from '../../../types/Types';
import {LegendItem} from '../reportModels';
import './Legend.scss';

export interface LegendProps {
  hiddenLines: uuid[];
  toggleLine: OnClick;
  clearSelectedListItems: OnClick;
  deleteItem: OnClickWithId;
  legendItems: Normalized<LegendItem>;
}

const iconIndicatorStyle: React.CSSProperties = {
  display: 'table',
  width: 24,
  height: 24,
};

const legendGridStyle: React.CSSProperties = {
  ...gridStyle,
  borderTopLeftRadius: borderRadius,
  borderTopRightRadius: borderRadius,
  marginBottom: 24,
  width: 560,
  maxHeight: 0.75 * window.innerHeight
};

const renderFacility = ({dataItem: {facility}}: GridCellProps) =>
  facility ? <td className="left-most first-uppercase">{orUnknown(facility)}</td> : <td>-</td>;

const renderCity = ({dataItem: {city}}: GridCellProps) =>
  city ? <td className="first-uppercase">{orUnknown(city)}</td> : <td>-</td>;

const renderAddress = ({dataItem: {address}}: GridCellProps) =>
  address ? <td className="first-uppercase">{orUnknown(address)}</td> : <td>-</td>;

const renderMediumCell = (medium: Medium) =>
  Array.isArray(medium)
    ? medium.map((singleMedium: Medium, index) => (
      <IconIndicator
        key={`${singleMedium}-${index}`}
        medium={singleMedium}
        style={iconIndicatorStyle}
      />
    ))
    : <IconIndicator medium={medium} style={iconIndicatorStyle}/>;

const renderMedium = ({dataItem: {medium}}: GridCellProps) =>
  <td>{renderMediumCell(medium)}</td>;

export const Legend = ({
  clearSelectedListItems,
  hiddenLines,
  legendItems: {entities: {lines}},
  toggleLine,
  deleteItem
}: LegendProps) => {
  const renderRemoveAllButtonLink = () => (
    <ButtonLinkBlue onClick={clearSelectedListItems} className="k-link">
      {translate('remove all')}
    </ButtonLinkBlue>
  );

  const renderDeleteButton = ({dataItem: {id}}: GridCellProps) => {
    const checked = isDefined(hiddenLines.find((it) => it === id));
    const onDeleteItem = () => deleteItem(id);
    const onToggleItem = () => toggleLine(id);
    return (
      <td className="icon">
        <RowMiddle>
          <Row>
            <ButtonVisibility key={`checked-${id}-${checked}`} onClick={onToggleItem} checked={checked}/>
          </Row>
          <Row>
            <ButtonDelete onClick={onDeleteItem}/>
          </Row>
        </RowMiddle>
      </td>
    );
  };

  const data = React.useMemo<LegendItem[]>(() => toArray(lines), [lines]);

  return (
    <Column className="Legend">
      <Grid data={data} style={legendGridStyle}>
        <GridColumn
          field="facility"
          cell={renderFacility}
          title={translate('facility')}
          headerClassName="left-most"
          width={140}
        />
        <GridColumn
          field="city"
          cell={renderCity}
          title={translate('city')}
          width={110}
        />
        <GridColumn
          field="address"
          cell={renderAddress}
          title={translate('address')}
          width={150}
        />
        <GridColumn
          field="medium"
          cell={renderMedium}
          title={translate('medium')}
          width={80}
        />
        <GridColumn
          headerCell={renderRemoveAllButtonLink}
          headerClassName="Link"
          cell={renderDeleteButton}
          width={80}
        />
      </Grid>
    </Column>
  );
};
