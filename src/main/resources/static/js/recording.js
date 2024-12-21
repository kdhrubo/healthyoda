let mediaRecorder = null;
let audioChunks = [];
let recordingTimer = null;
const MAX_RECORDING_TIME = 60000; // 1 minute in milliseconds

// UI Elements
const recordInitial = document.getElementById('recordInitial');
const recordActive = document.getElementById('recordActive');
const recordReview = document.getElementById('recordReview');
const startButton = document.getElementById('startButton');
const stopButton = document.getElementById('stopButton');
const rerecordButton = document.getElementById('rerecordButton');
const audioPlayer = document.getElementById('audioPlayer');
const audioFile = document.getElementById('audioFile');

function showState(state) {
    recordInitial.style.display = state === 'initial' ? 'block' : 'none';
    recordActive.style.display = state === 'recording' ? 'block' : 'none';
    recordReview.style.display = state === 'review' ? 'block' : 'none';
}

function updateTimer(timeLeft) {
    const seconds = Math.ceil(timeLeft / 1000);
    const timerDisplay = document.querySelector('.recording-timer');
    if (timerDisplay) {
        timerDisplay.textContent = `${seconds}s remaining`;
    }
}

function startRecording(stream) {
    mediaRecorder = new MediaRecorder(stream);
    audioChunks = [];

    mediaRecorder.ondataavailable = (event) => {
        audioChunks.push(event.data);
    };

    mediaRecorder.onstop = () => {
        const audioBlob = new Blob(audioChunks, { type: 'audio/webm' });
        const audioUrl = URL.createObjectURL(audioBlob);
        
        // Setup audio player
        audioPlayer.src = audioUrl;

        // Attach file to form
        const audioFile = new File([audioBlob], 'recording.webm', { type: 'audio/webm' });
        const dataTransfer = new DataTransfer();
        dataTransfer.items.add(audioFile);
        document.getElementById('audioFile').files = dataTransfer.files;
        
        // Show review state
        showState('review');
        
        // Stop and release media stream
        stream.getTracks().forEach(track => track.stop());
        
        // Clear timer if it's still running
        if (recordingTimer) {
            clearTimeout(recordingTimer);
            recordingTimer = null;
        }
    };

    mediaRecorder.start();
    showState('recording');

    // Start timer for 1 minute
    let startTime = Date.now();
    let timeLeft = MAX_RECORDING_TIME;

    // Update timer display every second
    const timerInterval = setInterval(() => {
        if (mediaRecorder && mediaRecorder.state === 'recording') {
            timeLeft = MAX_RECORDING_TIME - (Date.now() - startTime);
            if (timeLeft <= 0) {
                clearInterval(timerInterval);
                if (mediaRecorder.state === 'recording') {
                    mediaRecorder.stop();
                }
            } else {
                updateTimer(timeLeft);
            }
        } else {
            clearInterval(timerInterval);
        }
    }, 100);

    // Set timeout to stop recording after 1 minute
    recordingTimer = setTimeout(() => {
        if (mediaRecorder && mediaRecorder.state === 'recording') {
            mediaRecorder.stop();
        }
    }, MAX_RECORDING_TIME);
}

startButton.addEventListener('click', async () => {
    try {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        startRecording(stream);
    } catch (err) {
        console.error('Error accessing microphone:', err);
        alert('Error accessing microphone. Please ensure you have given permission.');
    }
});

stopButton.addEventListener('click', () => {
    if (mediaRecorder && mediaRecorder.state !== 'inactive') {
        mediaRecorder.stop();
    }
});

rerecordButton.addEventListener('click', () => {
    showState('initial');
});
