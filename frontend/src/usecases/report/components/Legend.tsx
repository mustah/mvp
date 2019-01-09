import * as React from 'react';
import {ButtonDelete} from '../../../components/buttons/ButtonDelete';
import {ButtonLink} from '../../../components/buttons/ButtonLink';
import {ButtonVisibility} from '../../../components/buttons/ButtonVisibility';
import {IconIndicator} from '../../../components/icons/IconIndicator';
import {Row} from '../../../components/layouts/row/Row';
import {Table, TableColumn} from '../../../components/table/Table';
import {TableHead} from '../../../components/table/TableHead';
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
  deleteItem: OnClickWithId;
  legendItems: Normalized<LegendItem>;
}

const iconIndicatorStyle: React.CSSProperties = {
  display: 'table',
  width: 24,
  height: 24,
};

const renderFacility = ({facility}: LegendItem) => facility ? orUnknown(facility) : '';

const renderAddress = ({address}: LegendItem) => address ? orUnknown(address) : '';

const renderCity = ({city}: LegendItem) => city ? orUnknown(city) : '';

const renderMedium = ({medium}: LegendItem) =>
  Array.isArray(medium)
    ? medium.map((singleMedium: Medium, index) => (
      <IconIndicator
        key={`${singleMedium}-${index}`}
        medium={singleMedium}
        style={iconIndicatorStyle}
      />
    ))
    : <IconIndicator medium={medium} style={iconIndicatorStyle}/>;

export const Legend = ({hiddenLines, legendItems, toggleLine, deleteItem}: LegendProps) => {

  const renderVisibilityButton = ({id}: LegendItem) => {
    const checked = isDefined(hiddenLines.find((it) => it === id));
    return <ButtonVisibility key={`checked-${id}-${checked}`} onClick={toggleLine} id={id} checked={checked}/>;
  };

  const renderDeleteButton = ({id}: LegendItem) => <ButtonDelete onClick={deleteItem} id={id}/>;

  return (
    <Row className="Legend">
      <Table result={legendItems.result} entities={legendItems.entities.lines}>
        <TableColumn
          header={<TableHead className="first">{translate('facility')}</TableHead>}
          cellClassName={'first first-uppercase'}
          renderCell={renderFacility}
        />
        <TableColumn
          header={<TableHead>{translate('city')}</TableHead>}
          cellClassName={'first-uppercase'}
          renderCell={renderCity}
        />
        <TableColumn
          header={<TableHead>{translate('address')}</TableHead>}
          cellClassName={'first-uppercase'}
          renderCell={renderAddress}
        />
        <TableColumn
          header={<TableHead>{translate('medium')}</TableHead>}
          cellClassName={'icon'}
          renderCell={renderMedium}
        />
        <TableColumn
          header={<TableHead className="icon"/>}
          cellClassName="icon"
          renderCell={renderVisibilityButton}
        />
        <TableColumn
          header={<TableHead className="Link"><ButtonLink colorClassName="Blue" onClick={() => console.log('clear')}>{translate('remove all')}</ButtonLink></TableHead>}
          cellClassName="icon"
          renderCell={renderDeleteButton}
        />
      </Table>
    </Row>
  );
};
