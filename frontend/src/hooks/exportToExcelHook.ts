import {ExcelExport} from '@progress/kendo-react-excel-export';
import * as React from 'react';
import {Callback} from '../types/Types';

export interface ExportToExcelProps {
  isExportingToExcel: boolean;
  exportToExcelSuccess: Callback;
  save: (exporter: React.RefObject<ExcelExport>) => void;
}

export const useExportToExcel = ({
  isExportingToExcel,
  exportToExcelSuccess,
  save,
}: ExportToExcelProps): React.RefObject<ExcelExport> => {
  const exporter = React.useRef<ExcelExport>(null);
  React.useEffect(() => {
    if (isExportingToExcel) {
      save(exporter);
      exportToExcelSuccess();
    }
  }, [isExportingToExcel]);
  return exporter;
};
