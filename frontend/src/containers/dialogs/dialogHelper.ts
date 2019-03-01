import {DateRange} from '../../components/dates/dateModels';
import {ExistingReadings, Readings} from '../../state/ui/graph/measurement/measurementModels';
import {UnixTimestamp} from '../../types/Types';

interface ReadingArguments {
  existingReadings: ExistingReadings;
  dateRange: DateRange;
  readIntervalMinutes?: number;
}

export const fillMissingMeasurements =
  ({existingReadings, readIntervalMinutes, dateRange: {start, end}}: ReadingArguments): Readings => {
    if (!readIntervalMinutes) {
      return existingReadings;
    }

    const startInSeconds = (start.valueOf() / 1000);
    const numRows = (end.valueOf() - start.valueOf()) / (1000 * readIntervalMinutes * 60);
    const withMissingReadings: Readings = {...existingReadings};

    for (let row = 0; row < numRows; row++) {
      const timestamp: UnixTimestamp = startInSeconds + row * readIntervalMinutes * 60;
      if (!withMissingReadings[timestamp]) {
        withMissingReadings[timestamp] = {id: timestamp};
      }
    }
    return withMissingReadings;
  };
